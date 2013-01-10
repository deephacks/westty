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
package org.deephacks.westty.job;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.deephacks.tools4j.config.RuntimeContext;
import org.deephacks.tools4j.config.internal.core.ConfigCdiExtension;
import org.deephacks.tools4j.config.model.AbortRuntimeException;
import org.deephacks.tools4j.config.model.Events;
import org.deephacks.tools4j.config.model.Lookup;
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

public class JobScheduler implements Extension {
    private static final Logger log = LoggerFactory.getLogger(JobScheduler.class);
    private static final String JOB_CLASS_KEY = "JOB_ID_KEY";
    private static final String LAST_EXECUTION_TIMESTAMP = "LAST_EXECUTION_TIMESTAMP";
    private static final Logger logger = LoggerFactory.getLogger(JobScheduler.class);
    private static final RuntimeContext ctx = Lookup.get().lookup(RuntimeContext.class);
    private static BeanManager beanManager;
    private Scheduler scheduler;
    /** config extension must first register schema, but ordering is not supported */
    private ConfigCdiExtension configExtension = new ConfigCdiExtension();
    private static final Set<Class<? extends Job>> jobs = new HashSet<Class<? extends Job>>();

    public JobScheduler() throws SchedulerException {
        StdSchedulerFactory factory = new org.quartz.impl.StdSchedulerFactory();
        this.scheduler = factory.getScheduler();

    }

    public void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager manager) {
        configExtension.afterBeanDiscovery(event, manager);
        StdSchedulerFactory factory = new StdSchedulerFactory();
        JobSchedulerConfig config = ctx.singleton(JobSchedulerConfig.class);
        try {
            factory.initialize(config.getInputStream());
            scheduler = factory.getScheduler();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }

    }

    public void start(@Observes AfterDeploymentValidation event, BeanManager bm) {
        beanManager = bm;
        try {
            scheduler.start();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    public void shutdown(@Observes BeforeShutdown event) {
        try {
            scheduler.shutdown(true);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    public void reschedule() {
        try {
            for (Class<? extends Job> cls : jobs) {
                String id = cls.getName();

                TriggerKey triggerKey = new TriggerKey(id);
                JobKey jobKey = new JobKey(id);
                Trigger trigger = newTrigger().withIdentity(triggerKey)
                        .withSchedule(cronSchedule(getCron(cls))).forJob(jobKey).build();
                logger.debug("Reschedule {}", id);
                scheduler.rescheduleJob(triggerKey, trigger);
            }
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    public void containerSchedule(@Observes ProcessAnnotatedType<?> pat) {
        configExtension.processAnnotatedType(pat);
        // extension wont give us config within same jar
        // need to register schema manually
        ctx.register(JobSchedulerConfig.class);
        ctx.register(JobConfig.class);
        AnnotatedType<?> t = pat.getAnnotatedType();
        Schedule schedule = t.getAnnotation(Schedule.class);
        if (schedule == null) {
            return;
        }
        Class<? extends Job> cls = t.getJavaClass().asSubclass(Job.class);
        jobs.add(cls);
        try {
            schedule(cls);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    public void schedule(Class<? extends Job> cls) throws SchedulerException {
        String id = cls.getName();
        try {
            getConfig(id);
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

        Trigger trigger = newTrigger().withIdentity(triggerKey).withSchedule(cronSchedule(cron))
                .forJob(jobdetail).build();
        scheduler.scheduleJob(jobdetail, trigger);
    }

    public JobConfig getConfig(String id) {
        return ctx.get(id, JobConfig.class);
    }

    private String getCron(Class<? extends Job> cls) {
        String cron = getConfig(cls.getName()).cronExpression;
        if (Strings.isNullOrEmpty(cron)) {
            cron = cls.getAnnotation(Schedule.class).value();
        }
        return cron;
    }

    @PersistJobDataAfterExecution
    @DisallowConcurrentExecution
    @SuppressWarnings({ "rawtypes", "unchecked" })
    static class JobDelegate implements org.quartz.Job {

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
