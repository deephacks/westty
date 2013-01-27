package org.deephacks.westty.spi;

public interface WesttyConnector extends WesttyModule {

    public Object NettyChannelPipelineFactory();
}
