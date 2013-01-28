package org.deephacks.westty.protobuf;

import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import org.deephacks.tools4j.config.RuntimeContext;
import org.deephacks.tools4j.config.model.Lookup;
import org.deephacks.westty.config.ProtobufConfig;
import org.deephacks.westty.spi.WesttyConnector;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WesttyProtobufModule implements WesttyConnector {
    private static final Logger log = LoggerFactory.getLogger(WesttyProtobufModule.class);
    private static final RuntimeContext ctx = Lookup.get().lookup(RuntimeContext.class);
    private static final ProtobufConfig config = ctx.singleton(ProtobufConfig.class);
    private static Channel CHANNEL;
    private static ServerBootstrap BOOTSTRAP;
    private static WesttyProtobufPipelineFactory FACTORY = new WesttyProtobufPipelineFactory();

    @Override
    public Object startup(ExecutorService bossExecutor, ExecutorService ioExecutor, Properties props) {
        BOOTSTRAP = new ServerBootstrap(new NioServerSocketChannelFactory(bossExecutor, ioExecutor,
                config.getIoWorkers()));
        BOOTSTRAP.setPipelineFactory(FACTORY);
        BOOTSTRAP.setOption("child.tcpNoDelay", true);
        BOOTSTRAP.setOption("child.keepAlive", true);
        CHANNEL = BOOTSTRAP.bind(new InetSocketAddress(config.getPort()));
        log.info("Protobuf listening on port {}.", config.getPort());
        return FACTORY;
    }

    @Override
    public void shutdown() {
        if (CHANNEL != null) {
            CHANNEL.close().awaitUninterruptibly(5000);
        }
        if (BOOTSTRAP != null) {
            BOOTSTRAP.releaseExternalResources();
        }
    }

    @Override
    public int getLoadOrder() {
        return 500;
    }

}
