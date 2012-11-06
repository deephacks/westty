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

@Config(name = WebConfig.ID, desc = WebConfig.DESC)
@ConfigScope
public class WebConfig {
    private static final String userDir = System.getProperty("user.dir");

    @Id(desc = WebConfig.DESC)
    static final String DESC = "Westty static web configuration. Changes requires server restart.";

    public static final String ID = "westty.web";

    @Config(desc = "URI where static web content is mounted.")
    @NotNull
    private String uri = "/";

    @Config(desc = "Path on file system where static web content is served from.")
    @NotNull
    private String staticRoot = userDir;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getStaticRoot() {
        return staticRoot;
    }

    public void setStaticRoot(String staticRoot) {
        this.staticRoot = staticRoot;
    }
}
