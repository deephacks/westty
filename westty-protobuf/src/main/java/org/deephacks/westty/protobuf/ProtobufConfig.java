package org.deephacks.westty.protobuf;

import org.deephacks.tools4j.config.Config;
import org.deephacks.tools4j.config.ConfigScope;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Config(name = "westty.protobuf",
        desc = "Westty protobuf configuration. Changes requires server restart.")
@ConfigScope
public class ProtobufConfig {

    public static int DEFAULT_PORT = 7777;

    @Config(desc = "Protobuf listening port.")
    @NotNull
    @Size(min = 0, max = 65535)
    private Integer port = DEFAULT_PORT;

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
