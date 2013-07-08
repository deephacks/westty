package org.deephacks.westty.config;

import org.deephacks.tools4j.config.Config;
import org.deephacks.tools4j.config.ConfigScope;
import org.deephacks.tools4j.config.Id;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@ConfigScope
@Config(name = "westty.protobuf",
        desc = "Westty protobuf configuration. Changes requires server restart.")
public class ProtobufConfig {

    public static int DEFAULT_PORT = 7777;

    @Id(desc="Name of this server")
    private String serverName = ServerConfig.DEFAULT_SERVER_NAME;

    public ProtobufConfig(){
    }

    public ProtobufConfig(String serverName){
        this.serverName = serverName;
    }

    @Config(desc = "Protobuf listening port.")
    @NotNull
    @Min(0)
    @Max(65535)
    private Integer port = DEFAULT_PORT;

    @Config(desc = "Specify the worker count to use. "
            + "See netty javadoc NioServerSocketChannelFactory.")
    @Min(1)
    @NotNull
    private Integer ioWorkerCount = Runtime.getRuntime().availableProcessors() * 2;

    public Integer getIoWorkerCount() {
        return ioWorkerCount;
    }

    public int getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setIoWorkerCount(Integer ioWorkerCount) {
        this.ioWorkerCount = ioWorkerCount;
    }
}
