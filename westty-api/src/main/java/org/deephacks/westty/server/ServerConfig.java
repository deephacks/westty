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
package org.deephacks.westty.server;

import org.deephacks.tools4j.config.Config;
import org.deephacks.tools4j.config.ConfigScope;
import org.deephacks.westty.ExecutorConfig;
import org.deephacks.westty.WebConfig;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@ConfigScope
@Config(name = "westty",
        desc = "Westty server engine configuration. Changes requires server restart.")
public class ServerConfig {

    @Config(desc = "Http listening port.")
    @NotNull
    @Size(min = 0, max = 65535)
    private Integer httpPort = 8080;

    @Config(desc = "Specify the worker count to use. "
            + "See netty javadoc NioServerSocketChannelFactory.")
    @Size(min = 1)
    @NotNull
    private Integer ioWorkerCount = Runtime.getRuntime().availableProcessors() * 2;

    @Config(desc = "Set the max request size in bytes. If this size exceeded "
            + "\"413 Request Entity Too Large\" willl be sent to the client.")
    @NotNull
    @Size(min = 4096)
    private Integer maxRequestSize = 1024 * 1024 * 10;

    @Config(desc = "Maximum byte length of aggregated http content. "
            + "TooLongFrameException raised if length exceeded.")
    @NotNull
    @Size(min = 16384)
    private Integer maxHttpContentChunkLength = 65536;

    @Config(desc = "Static web configuration.")
    private WebConfig web;

    @Config(desc = "Thread pool executor configuration.")
    private ExecutorConfig executor;

    public ServerConfig() {

    }

    public Integer getIoWorkerCount() {
        return ioWorkerCount;
    }

    public Integer getMaxRequestSize() {
        return maxRequestSize;
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public Integer getMaxHttpContentChunkLength() {
        return maxHttpContentChunkLength;
    }

    public WebConfig getWeb() {
        return web;
    }

    public ExecutorConfig getExecutor() {
        return executor;
    }
}
