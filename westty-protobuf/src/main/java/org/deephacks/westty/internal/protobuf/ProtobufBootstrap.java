package org.deephacks.westty.internal.protobuf;

import java.net.InetSocketAddress;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.deephacks.westty.protobuf.ProtobufConfig;
import org.deephacks.westty.spi.WesttyIoExecutors;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class ProtobufBootstrap {
    private static final Logger log = LoggerFactory.getLogger(ProtobufBootstrap.class);

    private Channel channel;
    private ServerBootstrap bootstrap;

    @Inject
    public ProtobufBootstrap(ProtobufConfig config, WesttyIoExecutors executors,
            ProtobufPipelineFactory factory) {
        bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(executors.getBoss(),
                executors.getWorker(), config.getIoWorkerCount()));
        bootstrap.setPipelineFactory(factory);
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        channel = bootstrap.bind(new InetSocketAddress(config.getPort()));
        log.info("Protobuf listening on port {}.", config.getPort());

    }

    public void shutdown(@Observes BeforeShutdown event) {
        if (channel != null) {
            channel.close().awaitUninterruptibly(5000);
        }
        if (bootstrap != null) {
            bootstrap.releaseExternalResources();
        }
    }

}
