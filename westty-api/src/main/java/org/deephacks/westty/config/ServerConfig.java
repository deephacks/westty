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

import com.google.common.base.Strings;
import org.deephacks.tools4j.config.Config;
import org.deephacks.tools4j.config.ConfigScope;
import org.deephacks.tools4j.config.Id;
import org.deephacks.tools4j.config.model.SystemProperties;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.File;

import static org.deephacks.westty.WesttyProperties.*;

@ConfigScope
@Config(name = "westty.server",
        desc = "Westty server engine configuration. Changes requires server restart.")
public class ServerConfig {

    public static final String PUBLIC_IP_PROP = "westty.public_ip";
    public static final String PRIVATE_IP_PROP = "westty.private_ip";
    public static final String CLUSTER_PORT_PROP = "westty.cluster.port";
    public static final String LIB_DIR_PROP = "westty.lib.dir";
    public static final String CONF_DIR_PROP = "westty.conf.dir";
    public static final String BIN_DIR_PROP = "westty.bin.dir";
    public static final String HTML_DIR_PROP = "westty.html.dir";

    public static final String DEFAULT_SERVER_NAME = "server";
    public static final String DEFAULT_CONF_DIR = "conf";
    public static final String DEFAULT_LIB_DIR = "lib";
    public static final String DEFAULT_BIN_DIR = "bin";
    public static final String DEFAULT_HTML_DIR = "html";
    public static final String DEFAULT_IP_ADDRESS = "127.0.0.1";
    public static final int DEFAULT_HTTP_PORT = 8080;
    public static final int DEFAULT_CLUSTER_PORT = 5701;

    @Id(desc="Name of this server")
    private String serverName = DEFAULT_SERVER_NAME;

    @Config(desc = "Public Ip Address.")
    private String publicIp;

    @Config(desc = "Private Ip Address.")
    // westty.server.instanceId.privateIp
    private String privateIp;

    @Config(desc="Port that this server uses for the cluster.")
    private Integer clusterPort;

    @Config(desc="Ip address that this server uses for the cluster.")
    private String clusterIpAddress;

    @Config(desc = "Location of the conf directory relative to the root directory.")
    private String confDir;

    @Config(desc = "Location of the lib directory relative to the root directory.")
    private String libDir;

    @Config(desc = "Location of the bin directory relative to the root directory.")
    private String binDir;

    @Config(desc = "Location of the html directory relative to the root directory.")
    private String htmlDir;


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
        this.serverName = serverName;
    }

    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }

    public void setIoWorkerCount(Integer ioWorkerCount) {
        this.ioWorkerCount = ioWorkerCount;
    }

    public void setMaxRequestSize(Integer maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
    }

    public void setMaxHttpContentChunkLength(Integer maxHttpContentChunkLength) {
        this.maxHttpContentChunkLength = maxHttpContentChunkLength;
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

    public void setPublicIp(String ip){
        this.privateIp = ip;
    }

    public static void setPublicIpProperty(String ip) {
        setProperty(PUBLIC_IP_PROP, ip);
    }

    public String getPublicIp() {
        if(publicIp != null) {
            return publicIp;
        }
        String value = getProperty(PUBLIC_IP_PROP);
        if (!Strings.isNullOrEmpty(value)) {
            return value;
        }
        return DEFAULT_IP_ADDRESS;
    }

    public void setPrivateIp(String ip){
        this.privateIp = ip;
    }

    public static void setPrivateIpProperty(String ip) {
        setProperty(PRIVATE_IP_PROP, ip);
    }

    public String getPrivateIp() {
        if(privateIp != null) {
            return privateIp;
        }
        String value = getProperty(PRIVATE_IP_PROP);
        if (!Strings.isNullOrEmpty(value)) {
            return value;
        }
        return getPublicIp();
    }

    public void setClusterPort(int port){
        this.clusterPort = port;
    }

    public int getClusterPort() {
        if(clusterPort != null){
            return clusterPort;
        }
        String value = getProperty(CLUSTER_PORT_PROP);
        if (!Strings.isNullOrEmpty(value)) {
            return Integer.parseInt(value);
        }
        return DEFAULT_CLUSTER_PORT;
    }

    public static void setClusterPortProperty(int port) {
        setProperty(CLUSTER_PORT_PROP, Integer.toString(port));
    }


    public static void initRootDir(File root) {
        if (!root.exists()) {
            return;
        }
        setBinDirProperty(new File(root, DEFAULT_BIN_DIR));
        setLibDirProperty(new File(root, DEFAULT_LIB_DIR));
        setConfDirProperty(new File(root, DEFAULT_CONF_DIR));
        setHtmlDirProperty(new File(root, DEFAULT_HTML_DIR));
        File confDir = new ServerConfig().getConfDir();
        File propFile = new File(confDir, WESTTY_PROPERTIES_FILE);
        loadProperties(propFile);
        SystemProperties.add(getProperties());
    }

    public void setLibDir(String dir){
        this.libDir = dir;
    }

    public static void setLibDirProperty(File dir) {
        setProperty(LIB_DIR_PROP, dir.getAbsolutePath());
    }

    public File getLibDir() {
        if(libDir != null) {
            return new File(libDir);
        }
        String value = getProperty(LIB_DIR_PROP);
        return value == null ? null : new File(getProperty(LIB_DIR_PROP));
    }

    public static void setConfDirProperty(File dir) {
        setProperty(CONF_DIR_PROP, dir.getAbsolutePath());
    }

    public void setConfDir(String dir){
        this.confDir = dir;
    }

    public File getConfDir() {
        if(confDir != null) {
            return new File(confDir);
        }
        String value = getProperty(CONF_DIR_PROP);
        return value == null ? null : new File(getProperty(CONF_DIR_PROP));
    }

    public void setBinDir(String dir){
        this.binDir = dir;
    }

    public static void setBinDirProperty(File dir) {
        setProperty(BIN_DIR_PROP, dir.getAbsolutePath());
    }

    public File getBinDir() {
        if(binDir != null) {
            return new File(binDir);
        }
        String value = getProperty(BIN_DIR_PROP);
        return value == null ? null : new File(getProperty(BIN_DIR_PROP));
    }

    public static void setHtmlDirProperty(File dir) {
        setProperty(HTML_DIR_PROP, dir.getAbsolutePath());
    }

    public void setHtmlDir(String dir){
        this.htmlDir = dir;
    }

    public File getHtmlDir() {
        if(htmlDir != null){
            return new File(htmlDir);
        }
        String value = getProperty(HTML_DIR_PROP);
        return value == null ? null : new File(getProperty(HTML_DIR_PROP));
    }
}
