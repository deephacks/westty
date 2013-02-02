/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.deephacks.westty.internal.job;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

import org.deephacks.tools4j.config.RuntimeContext;
import org.deephacks.tools4j.config.model.AbortRuntimeException;
import org.deephacks.tools4j.config.model.Events;
import org.deephacks.tools4j.config.model.Lookup;
import org.deephacks.westty.job.Job;
import org.deephacks.westty.job.JobConfig;
import org.deephacks.westty.job.JobSchedulerConfig;
import org.deephacks.westty.job.Schedule;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;

@Singleton
public class JobScheduler extends StdSchedulerFactory {
    private static final Logger log = LoggerFactory.getLogger(JobScheduler.class);
    private static final String JOB_CLASS_KEY = "JOB_ID_KEY";
    private static final String LAST_EXECUTION_TIMESTAMP = "LAST_EXECUTION_TIMESTAMP";
    private static final RuntimeContext ctx = Lookup.get().lookup(RuntimeContext.class);
    private Scheduler scheduler;
    private final JobThreadPool threadPool;
    private final JobConnectionProvider provider;
    private final JobExecutor executor;

    @Inject
    public JobScheduler(JobThreadPool threadPool, JobExecutor executor, DataSource dataSource) {
        this.threadPool = threadPool;
        this.provider = new JobConnectionProvider(dataSource);
        this.executor = executor;
    }

    public void start() throws SchedulerException {
        JobSchedulerConfig config = ctx.singleton(JobSchedulerConfig.class);
        scheduler = config.getScheduler(executor, provider, threadPool);

        try {
            scheduler.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown() throws SchedulerException {
        scheduler.shutdown(true);
    }

    public void reschedule() {
        try {
            for (Class<? extends Job> cls : JobExtension.getJobs()) {
                String id = cls.getName();

                TriggerKey triggerKey = new TriggerKey(id);
                JobKey jobKey = new JobKey(id);
                Trigger trigger = newTrigger().withIdentity(triggerKey)
                        .withSchedule(cronSchedule(getCron(cls))).forJob(jobKey).build();
                log.debug("Reschedule {}", id);
                scheduler.rescheduleJob(triggerKey, trigger);
            }
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    public void schedule() throws SchedulerException {
        for (Class<? extends Job> cls : JobExtension.getJobs()) {
            String id = cls.getName();
            try {
                ctx.get(id, JobConfig.class);
            } catch (AbortRuntimeException e) {
                if (e.getEvent().getCode() == Events.CFG304) {
                    ctx.registerDefault(new JobConfig(id));
                }
            }
            TriggerKey triggerKey = new TriggerKey(cls.getName());
            JobKey jobKey = new JobKey(cls.getName());

            JobDetail jobdetail = newJob(JobDelegate.class).usingJobData(JOB_CLASS_KEY, id)
                    .withIdentity(jobKey).build();
            String cron = getCron(cls);

            Trigger trigger = newTrigger().withIdentity(triggerKey)
                    .withSchedule(cronSchedule(cron)).forJob(jobdetail).build();
            scheduler.scheduleJob(jobdetail, trigger);
        }
    }

    private String getCron(Class<? extends Job> cls) {
        String cron = ctx.get(cls.getName(), JobConfig.class).cronExpression;
        if (Strings.isNullOrEmpty(cron)) {
            cron = cls.getAnnotation(Schedule.class).value();
        }
        return cron;
    }

    @PersistJobDataAfterExecution
    @DisallowConcurrentExecution
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static class JobDelegate implements org.quartz.Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            JobDataMap map = context.getJobDetail().getJobDataMap();
            if (map.get(LAST_EXECUTION_TIMESTAMP) != null) {
                long ts = Long.parseLong(map.getString(LAST_EXECUTION_TIMESTAMP));
                if (System.currentTimeMillis() - ts < 2000) {
                    return;
                }
            }
            String className = map.getString(JOB_CLASS_KEY);
            Class<? extends Job> cls;
            try {
                cls = Class.forName(className).asSubclass(Job.class);
            } catch (ClassNotFoundException e) {
                throw new JobExecutionException(e);
            }
            Logger logger = LoggerFactory.getLogger(cls);
            BeanManager beanManager = JobExtension.getBeanManager();
            Set<Bean<?>> jobBeans = beanManager.getBeans(cls);

            Bean jobBean = beanManager.resolve(jobBeans);
            CreationalContext cc = beanManager.createCreationalContext(jobBean);
            Job job = (Job) beanManager.getReference(jobBean, Job.class, cc);
            try {
                Stopwatch s = new Stopwatch().start();
                try {
                    logger.debug("Executing.");
                    job.execute(new JobDataImpl(map));
                    map.putAsString(LAST_EXECUTION_TIMESTAMP, System.currentTimeMillis());
                } catch (Exception e) {
                    logger.warn("Unexpected exception", e);
                }
                logger.debug("Execution took " + s.elapsedTime(TimeUnit.NANOSECONDS) + "ns");
            } finally {
                jobBean.destroy((Object) job, cc);
            }
        }
    }

}
