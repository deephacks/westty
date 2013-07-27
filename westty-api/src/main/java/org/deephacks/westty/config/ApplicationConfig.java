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

import org.deephacks.confit.Config;
import org.deephacks.confit.Id;

import javax.validation.constraints.NotNull;

@Config(name = "application", desc = "application")
public class ApplicationConfig {
    @Id(desc = "westty application identification")
    private String id;

    @Config(desc = "URI where application is mounted.")
    @NotNull
    private String appUri = "/";

    @Config(desc = "URI where jaxrs services are mounted.")
    @NotNull
    private String jaxrsUri = "/";

    @Config(desc = "URI where static web content is mounted.")
    @NotNull
    private String staticUri = "/";

    @Config(desc = "Path on file system where static web content is served from.")
    @NotNull
    private String staticRoot;

    @Config(desc = "URI where websockets are mounted.")
    @NotNull
    private String websocketUri = "/";

    public ApplicationConfig() {
    }

    public ApplicationConfig(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppUri() {
        return appUri;
    }

    public void setAppUri(String appUri) {
        this.appUri = appUri;
    }

    public String getJaxrsUri() {
        return jaxrsUri;
    }

    public void setJaxrsUri(String jaxrsUri) {
        this.jaxrsUri = jaxrsUri;
    }

    public String getStaticUri() {
        return staticUri;
    }

    public void setStaticUri(String staticUri) {
        this.staticUri = staticUri;
    }

    public String getStaticRoot() {
        return staticRoot;
    }

    public void setStaticRoot(String staticRoot) {
        this.staticRoot = staticRoot;
    }

    public String getWebsocketUri() {
        return websocketUri;
    }

    public void setWebsocketUri(String websocketUri) {
        this.websocketUri = websocketUri;
    }

}
