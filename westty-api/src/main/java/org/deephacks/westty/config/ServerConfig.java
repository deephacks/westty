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
import org.deephacks.tools4j.config.Id;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.File;

@ConfigScope
@Config(name = "servers",
        desc = "Server engine configuration. Changes requires server restart.")
public class ServerConfig {

    public static final String DEFAULT_SERVER_NAME = "server";
    public static final String DEFAULT_CONF_DIR = "conf";
    public static final String DEFAULT_LIB_DIR = "lib";
    public static final String DEFAULT_BIN_DIR = "bin";
    public static final String DEFAULT_HTML_DIR = "html";
    public static final String DEFAULT_IP_ADDRESS = "127.0.0.1";
    public static final int DEFAULT_HTTP_PORT = 8080;
    public static final int DEFAULT_CLUSTER_PORT = 5701;

    @Id(desc="Name of this server")
    private String name = DEFAULT_SERVER_NAME;

    @Config(desc = "Public Ip Address.")
    private String publicIp = DEFAULT_IP_ADDRESS;

    @Config(desc = "Private Ip Address.")
    private String privateIp = DEFAULT_IP_ADDRESS;

    @Config(desc="Port that this server uses for the cluster.")
    private Integer clusterPort = DEFAULT_CLUSTER_PORT;

    @Config(desc="Ip address that this server uses for the cluster.")
    private String clusterIp = DEFAULT_IP_ADDRESS;

    @Config(desc = "Root directory.")
    private String rootDir = System.getProperty("root.dir");

    @Config(desc = "Location of the conf directory relative to the root directory.")
    private String confDir = DEFAULT_CONF_DIR;

    @Config(desc = "Location of the lib directory relative to the root directory.")
    private String libDir = DEFAULT_LIB_DIR;

    @Config(desc = "Location of the bin directory relative to the root directory.")
    private String binDir = DEFAULT_BIN_DIR;

    @Config(desc = "Location of the html directory relative to the root directory.")
    private String htmlDir = DEFAULT_HTML_DIR;


    @Config(desc = "Http listening port.")
    @NotNull
    @Min(0)
    @Max(65535)
    private Integer httpPort = DEFAULT_HTTP_PORT;

    @Config(desc = "Specify the worker count to use. "
            + "See netty javadoc NioServerSocketChannelFactory.")
    @Min(1)
    @NotNull
    private Integer ioWorkerCount = Runtime.getRuntime().availableProcessors() * 2;

    @Config(desc = "Set the max request size in bytes. If this size exceeded "
            + "\"413 Request Entity Too Large\" willl be sent to the client.")
    @NotNull
    @Min(4096)
    private Integer maxRequestSize = 1024 * 1024 * 10;

    @Config(desc = "Maximum byte length of aggregated http content. "
            + "TooLongFrameException raised if length exceeded.")
    @NotNull
    @Min(16384)
    private Integer maxHttpContentChunkLength = 65536;

    public ServerConfig() {

    }

    public ServerConfig(String serverName) {
        this.name = serverName;
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

    public String getPublicIp() {
        return publicIp;
    }

    public String getPrivateIp() {
        return privateIp;
    }

    public int getClusterPort() {
        return clusterPort;
    }

    public File getLibDir() {
        return new File(rootDir, libDir);
    }

    public File getConfDir() {
        return new File(rootDir, confDir);
    }

    public File getBinDir() {
        return new File(rootDir, binDir);
    }

    public File getHtmlDir() {
        return new File(rootDir, htmlDir);
    }

    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }

    public void setServerName(String serverName) {
        this.name = serverName;
    }

    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
    }

    public void setPrivateIp(String privateIp) {
        this.privateIp = privateIp;
    }

    public void setClusterPort(Integer clusterPort) {
        this.clusterPort = clusterPort;
    }

    public void setClusterIpAddress(String clusterIpAddress) {
        this.clusterIp = clusterIpAddress;
    }

}
