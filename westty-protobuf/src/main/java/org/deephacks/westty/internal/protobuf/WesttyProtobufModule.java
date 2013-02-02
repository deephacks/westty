package org.deephacks.westty.internal.protobuf;

import java.net.InetSocketAddress;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.deephacks.tools4j.config.RuntimeContext;
import org.deephacks.tools4j.config.model.Lookup;
import org.deephacks.westty.persistence.Transactional;
import org.deephacks.westty.protobuf.ProtobufConfig;
import org.deephacks.westty.spi.WesttyIoExecutors;
import org.deephacks.westty.spi.WesttyModule;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class WesttyProtobufModule implements WesttyModule {
    private static final Logger log = LoggerFactory.getLogger(WesttyProtobufModule.class);
    private static final RuntimeContext ctx = Lookup.get().lookup(RuntimeContext.class);
    @Inject
    private WesttyProtobufPipelineFactory factory;
    @Inject
    private WesttyIoExecutors executors;

    private Channel channel;
    private ServerBootstrap bootstrap;

    @Override
    @Transactional
    public void startup() {
        ProtobufConfig config = ctx.singleton(ProtobufConfig.class);
        bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(executors.getBoss(),
                executors.getWorker(), config.getIoWorkerCount()));
        bootstrap.setPipelineFactory(factory);
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        channel = bootstrap.bind(new InetSocketAddress(config.getPort()));
        log.info("Protobuf listening on port {}.", config.getPort());
    }

    @Override
    public void shutdown() {
        if (channel != null) {
            channel.close().awaitUninterruptibly(5000);
        }
        if (bootstrap != null) {
            bootstrap.releaseExternalResources();
        }
    }

    @Override
    public int priority() {
        return 10000;
    }

}
