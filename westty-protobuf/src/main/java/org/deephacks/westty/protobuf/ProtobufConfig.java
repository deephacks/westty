package org.deephacks.westty.protobuf;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.deephacks.tools4j.config.Config;
import org.deephacks.tools4j.config.ConfigScope;

@Config(name = "westty.protobuf",
        desc = "Westty protobuf configuration. Changes requires server restart.")
@ConfigScope
public class ProtobufConfig {

    @Config(desc = "Protobuf listening port.")
    @NotNull
    @Size(min = 0, max = 65535)
    private Integer port = 7777;

    @Config(desc = "Specify the worker count to use. "
            + "See netty javadoc NioServerSocketChannelFactory.")
    @Size(min = 1)
    @NotNull
    private Integer ioWorkerCount = Runtime.getRuntime().availableProcessors() * 2;

    public Integer getIoWorkerCount() {
        return ioWorkerCount;
    }

    public int getPort() {
        return port;
    }

}
