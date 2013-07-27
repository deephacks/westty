package org.deephacks.westty.config;

import org.deephacks.confit.Config;
import org.deephacks.confit.ConfigScope;
import org.deephacks.confit.Id;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@ConfigScope
@Config(name = "protobuf",
        desc = "Protobuf configuration. Changes requires server restart.")
public class ProtobufConfig {

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
    private Integer port = 7777;

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
