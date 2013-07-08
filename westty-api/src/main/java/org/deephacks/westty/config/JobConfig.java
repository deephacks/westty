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
import org.deephacks.tools4j.config.Id;

@Config(name = "westty.jobs", desc = "Configuration for a specific job")
public class JobConfig {
    @Id(desc = "id")
    private String id;

    @Config(desc = "Cron expression used for job scheduling.")
    private String cronExpression;

    public JobConfig() {

    }

    public JobConfig(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getCronExpression() {
        return cronExpression;
    }
}