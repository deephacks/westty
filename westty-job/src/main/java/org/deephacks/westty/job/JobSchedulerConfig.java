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

import org.deephacks.tools4j.config.Config;
import org.deephacks.tools4j.config.ConfigScope;
import org.deephacks.tools4j.config.Id;
import org.deephacks.westty.internal.job.JobConnectionProvider;
import org.deephacks.westty.internal.job.JobThreadPool;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.core.QuartzScheduler;
import org.quartz.core.QuartzSchedulerResources;
import org.quartz.impl.StdJobRunShellFactory;
import org.quartz.impl.StdScheduler;
import org.quartz.impl.jdbcjobstore.JobStoreTX;
import org.quartz.impl.jdbcjobstore.Semaphore;
import org.quartz.impl.jdbcjobstore.UpdateLockRowSemaphore;
import org.quartz.simpl.CascadingClassLoadHelper;
import org.quartz.spi.ThreadExecutor;
import org.quartz.utils.DBConnectionManager;

@Config(name = JobSchedulerConfig.ID, desc = JobSchedulerConfig.DESC)
@ConfigScope
public class JobSchedulerConfig {

    static final String DESC = "Job scheduler configuration. Changes requires restart.";

    @Id(desc = JobSchedulerConfig.DESC)
    public static final String ID = "westty.job";

    @Config(desc = "See org.quartz.scheduler.instanceName")
    private String instanceName = "WesttyQuartzScheduler";

    @Config(desc = "See org.quartz.scheduler.instanceId")
    private String instanceId = "AUTO";

    @Config(desc = "See org.quartz.jobStore.isClustered")
    private Boolean isClustered = true;

    @Config(desc = "See org.quartz.scheduler.idleWaitTime")
    private Long idleTimeWait = 5000L;

    @Config(desc = "See org.quartz.scheduler.dbFailureRetryInterval")
    private Long dbFailureRetryInterval = 15000L;

    @Config(desc = "See org.quartz.jobStore.clusterCheckinInterval")
    private Long clusterCheckinInterval = 7500L;

    @Config(desc = "See org.quartz.scheduler.batchTriggerAcquisitionMaxCount")
    private Integer batchTriggerAcquisitionMaxCount = 10;

    @Config(desc = "See org.quartz.scheduler.batchTriggerAcquisitionFireAheadTimeWindow")
    private Long batchTriggerAcquisitionFireAheadTimeWindow = 0L;

    public Scheduler getScheduler(ThreadExecutor executor, JobConnectionProvider provider,
            JobThreadPool threadPool) throws SchedulerException {

        DBConnectionManager manager = DBConnectionManager.getInstance();
        manager.addConnectionProvider(provider.getDataSourceName(), provider);

        CascadingClassLoadHelper cl = new CascadingClassLoadHelper();

        QuartzSchedulerResources resources = new QuartzSchedulerResources();
        resources.setInstanceId(instanceId);
        resources.setName(instanceName);
        resources.setMakeSchedulerThreadDaemon(true);
        resources.setThreadName(instanceName);
        resources.setThreadPool(threadPool);
        resources.setThreadExecutor(executor);
        resources.setRunUpdateCheck(false);
        resources.setMaxBatchSize(batchTriggerAcquisitionMaxCount);
        resources.setBatchTimeWindow(batchTriggerAcquisitionFireAheadTimeWindow);

        QuartzScheduler qs = new QuartzScheduler(resources, idleTimeWait, dbFailureRetryInterval);
        Scheduler scheduler = new StdScheduler(qs);

        StdJobRunShellFactory jobShell = new StdJobRunShellFactory();
        resources.setJobRunShellFactory(jobShell);

        JobStoreTX store = new JobStoreTX();
        store.setLockHandler(getLockStrategy());
        store.setLockOnInsert(true);
        store.setInstanceName(instanceName);
        store.setInstanceId(instanceId);
        store.setIsClustered(isClustered);
        store.setClusterCheckinInterval(clusterCheckinInterval);
        store.setTxIsolationLevelSerializable(true);
        store.setDataSource(provider.getDataSourceName());
        resources.setJobStore(store);

        cl.initialize();
        jobShell.initialize(scheduler);
        store.initialize(cl, qs.getSchedulerSignaler());

        return scheduler;

    }

    public Semaphore getLockStrategy() {
        UpdateLockRowSemaphore lock = new UpdateLockRowSemaphore();
        lock.setSchedName(instanceName);
        return lock;
    }

}
