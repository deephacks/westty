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
package org.deephacks.westty.config;

import org.deephacks.tools4j.config.Config;
import org.deephacks.tools4j.config.ConfigScope;

@Config(name = "westty.job", desc = "Job scheduler configuration. Changes requires restart.")
@ConfigScope
public class JobSchedulerConfig {

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

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public Boolean getIsClustered() {
        return isClustered;
    }

    public void setIsClustered(Boolean isClustered) {
        this.isClustered = isClustered;
    }

    public Long getIdleTimeWait() {
        return idleTimeWait;
    }

    public void setIdleTimeWait(Long idleTimeWait) {
        this.idleTimeWait = idleTimeWait;
    }

    public Long getDbFailureRetryInterval() {
        return dbFailureRetryInterval;
    }

    public void setDbFailureRetryInterval(Long dbFailureRetryInterval) {
        this.dbFailureRetryInterval = dbFailureRetryInterval;
    }

    public Long getClusterCheckinInterval() {
        return clusterCheckinInterval;
    }

    public void setClusterCheckinInterval(Long clusterCheckinInterval) {
        this.clusterCheckinInterval = clusterCheckinInterval;
    }

    public Integer getBatchTriggerAcquisitionMaxCount() {
        return batchTriggerAcquisitionMaxCount;
    }

    public void setBatchTriggerAcquisitionMaxCount(Integer batchTriggerAcquisitionMaxCount) {
        this.batchTriggerAcquisitionMaxCount = batchTriggerAcquisitionMaxCount;
    }

    public Long getBatchTriggerAcquisitionFireAheadTimeWindow() {
        return batchTriggerAcquisitionFireAheadTimeWindow;
    }

    public void setBatchTriggerAcquisitionFireAheadTimeWindow(
            Long batchTriggerAcquisitionFireAheadTimeWindow) {
        this.batchTriggerAcquisitionFireAheadTimeWindow = batchTriggerAcquisitionFireAheadTimeWindow;
    }

}
