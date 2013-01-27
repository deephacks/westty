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

import static org.quartz.impl.StdSchedulerFactory.PROP_JOB_STORE_CLASS;
import static org.quartz.impl.StdSchedulerFactory.PROP_JOB_STORE_USE_PROP;
import static org.quartz.impl.StdSchedulerFactory.PROP_SCHED_INSTANCE_ID;
import static org.quartz.impl.StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME;
import static org.quartz.impl.StdSchedulerFactory.PROP_SCHED_SKIP_UPDATE_CHECK;
import static org.quartz.impl.StdSchedulerFactory.PROP_THREAD_POOL_CLASS;
import static org.quartz.impl.StdSchedulerFactory.PROP_THREAD_POOL_PREFIX;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.deephacks.tools4j.config.Config;
import org.deephacks.tools4j.config.ConfigScope;
import org.deephacks.tools4j.config.Id;
import org.deephacks.westty.config.JpaConfig;

@Config(name = JobSchedulerConfig.ID, desc = JobSchedulerConfig.DESC)
@ConfigScope
public class JobSchedulerConfig {

    static final String DESC = "Job scheduler configuration. Changes requires restart.";

    @Id(desc = JobSchedulerConfig.DESC)
    public static final String ID = "westty.job";

    @Config(desc = PROP_SCHED_INSTANCE_NAME)
    private String instanceName = "QuartzScheduler";

    @Config(desc = PROP_SCHED_INSTANCE_ID)
    private String instanceId = "AUTO";

    @Config(desc = PROP_THREAD_POOL_CLASS)
    private String threadPoolClass = "org.deephacks.westty.job.JobThreadPool";

    private static final String PROP_THREAD_POOL_THREAD_COUNT = PROP_THREAD_POOL_PREFIX
            + ".threadCount";

    @Config(desc = PROP_THREAD_POOL_THREAD_COUNT)
    private Integer threadCount = 25;

    @Config(desc = PROP_JOB_STORE_CLASS)
    private String jobStoreClass = "org.quartz.impl.jdbcjobstore.JobStoreTX";

    private static final String DRIVER_DELEGATE_CLASS = "org.quartz.jobStore.driverDelegateClass";
    @Config(desc = DRIVER_DELEGATE_CLASS)
    private String driverDelegateClass = "org.quartz.impl.jdbcjobstore.CloudscapeDelegate";

    @Config(desc = PROP_JOB_STORE_USE_PROP)
    private Boolean useProperties = false;

    private static final String PROP_IS_CLUSTERED = "org.quartz.jobStore.isClustered";
    @Config(desc = PROP_IS_CLUSTERED)
    private Boolean isClustered = true;

    private static final String PROP_CLUSTER_CHECK_INTERVAL = "org.quartz.jobStore.clusterCheckinInterval";
    @Config(desc = PROP_CLUSTER_CHECK_INTERVAL)
    private Integer clusterCheckinInterval = 20000;

    private static final String PROP_DATASOURCE = "org.quartz.jobStore.dataSource";
    @Config(desc = PROP_DATASOURCE)
    private String dataSource = "jobDataSource";

    @Config(desc = "")
    private JpaConfig jpa;

    private static final String PROP_DATASOURCE_DRIVER = "org.quartz.dataSource.%s.driver";
    private static final String PROP_URL = "org.quartz.dataSource.%s.URL";
    private static final String PROP_USER = "org.quartz.dataSource.%s.user";
    private static final String PROP_PASSWORD = "org.quartz.dataSource.%s.password";
    private static final String PROP_MAX_CONNECTIONS = "org.quartz.dataSource.%s.maxConnections";
    private static final String PROP_VALIDATION_QUERY = "org.quartz.dataSource.%s.validationQuery";

    private static final String NL = System.getProperty("line.separator");

    public InputStream getInputStream() {
        StringBuilder str = new StringBuilder();
        str.append(PROP_SCHED_SKIP_UPDATE_CHECK).append("=").append("true").append(NL);
        str.append(PROP_SCHED_INSTANCE_NAME).append("=").append(instanceName).append(NL);
        str.append(PROP_SCHED_INSTANCE_ID).append("=").append(instanceId).append(NL);
        str.append(PROP_THREAD_POOL_CLASS).append("=").append(threadPoolClass).append(NL);
        str.append(PROP_THREAD_POOL_THREAD_COUNT).append("=").append(threadCount).append(NL);
        str.append(PROP_JOB_STORE_CLASS).append("=").append(jobStoreClass).append(NL);
        str.append(DRIVER_DELEGATE_CLASS).append("=").append(driverDelegateClass).append(NL);
        str.append(PROP_IS_CLUSTERED).append("=").append(isClustered).append(NL);
        str.append(PROP_JOB_STORE_USE_PROP).append("=").append(useProperties).append(NL);
        str.append(PROP_CLUSTER_CHECK_INTERVAL).append("=").append(clusterCheckinInterval)
                .append(NL);
        str.append(PROP_DATASOURCE).append("=").append(dataSource).append(NL);
        String val = String.format(PROP_DATASOURCE_DRIVER, dataSource);
        str.append(val).append("=").append(jpa.getDriver()).append(NL);
        val = String.format(PROP_URL, dataSource);
        str.append(val).append("=").append(jpa.getUrl()).append(NL);
        val = String.format(PROP_USER, dataSource);
        str.append(val).append("=").append(jpa.getUser()).append(NL);
        val = String.format(PROP_PASSWORD, dataSource);
        str.append(val).append("=").append(jpa.getPassword()).append(NL);
        val = String.format(PROP_MAX_CONNECTIONS, dataSource);
        str.append(val).append("=").append("5").append(NL);
        val = String.format(PROP_VALIDATION_QUERY, dataSource);
        str.append(val).append("=").append("select 0 from dual").append(NL);
        return new ByteArrayInputStream(str.toString().getBytes());
    }
}
