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

import javax.validation.constraints.NotNull;

import org.deephacks.tools4j.config.Config;
import org.deephacks.tools4j.config.ConfigScope;
import org.deephacks.tools4j.config.Id;

@Config(name = JaxrsConfig.ID, desc = JaxrsConfig.DESC)
@ConfigScope
public class JaxrsConfig {

    static final String DESC = "Westty jaxrs configuration. Changes requires server restart.";

    @Id(desc = JaxrsConfig.DESC)
    public static final String ID = "westty.jaxrs";

    @Config(desc = "URI where jaxrs services listen.")
    @NotNull
    private String uri = "/jaxrs";

    public String getUri() {
        return uri;
    }

}
