package org.deephacks.westty.protobuf;

import java.util.Properties;

import org.deephacks.westty.spi.WesttyConnector;

public class WesttyProtobufModule implements WesttyConnector {

    @Override
    public void startup(Properties props) {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public int getLoadOrder() {
        return 0;
    }

    @Override
    public Object NettyChannelPipelineFactory() {
        return null;
    }
}
