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
package org.deephacks.westty.internal.protobuf;

import com.google.protobuf.MessageLite;
import org.deephacks.westty.config.ProtobufConfig;
import org.deephacks.westty.config.ServerSpecificConfigProxy;
import org.deephacks.westty.protobuf.FailureMessages.Failure;
import org.deephacks.westty.protobuf.ProtobufClient;
import org.deephacks.westty.protobuf.ProtobufSerializer;
import org.deephacks.westty.spi.ProviderShutdownEvent;
import org.deephacks.westty.spi.ProviderStartupEvent;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static org.jboss.netty.buffer.ChannelBuffers.wrappedBuffer;

@Singleton
class ProtobufPipelineFactory implements ChannelPipelineFactory {
    private static final Logger log = LoggerFactory.getLogger(ProtobufChannelHandler.class);

    @Inject
    private ProtobufChannelHandler channelHandler;

    private ProtobufSerializer serializer;
    private ThreadPoolExecutor executor;
    private ExecutionHandler executionHandler;
    private Channel channel;
    private ServerBootstrap bootstrap;
    private ProtobufConfig config;

    @Inject
    public ProtobufPipelineFactory(ServerSpecificConfigProxy<ProtobufConfig> config, ThreadPoolExecutor executor, ProtobufSerializer serializer) {
        this.config = config.get();
        this.executor = executor;
        this.serializer = serializer;
        ExecutorService workers = Executors.newCachedThreadPool();
        ExecutorService boss = Executors.newCachedThreadPool();
        bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(boss,
                workers, this.config.getIoWorkerCount()));
        bootstrap.setPipelineFactory(this);
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
    }

    public void startup(@Observes ProviderStartupEvent event) {
        channel = bootstrap.bind(new InetSocketAddress(config.getPort()));
        log.info("Protobuf listening on port {}.", config.getPort());
    }

    public void shutdown(@Observes ProviderShutdownEvent event) {
        log.info("Closing channels.");
        channelHandler.getClientChannels().close();
        if (channel != null) {
            channel.close().awaitUninterruptibly(5000);
        }
        if (bootstrap != null) {
            bootstrap.releaseExternalResources();
        }
        log.info("All channels closed.");
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        if (executionHandler == null) {
            this.executionHandler = new ExecutionHandler(executor);
        }
        return Channels.pipeline(new LengthFieldBasedFrameDecoder(
                ProtobufClient.MESSAGE_MAX_SIZE_10MB, 0, ProtobufClient.MESSAGE_LENGTH,
                0, ProtobufClient.MESSAGE_LENGTH),
                new WesttyProtobufDecoder(serializer), new LengthFieldPrepender(
                        ProtobufClient.MESSAGE_LENGTH),
                new WesttyProtobufEncoder(serializer), executionHandler, channelHandler);
    }

    private static class WesttyProtobufDecoder extends OneToOneDecoder {
        private final ProtobufSerializer serializer;

        public WesttyProtobufDecoder(ProtobufSerializer serializer) {
            this.serializer = serializer;
        }

        @Override
        protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg)
                throws Exception {
            if (!(msg instanceof ChannelBuffer)) {
                return msg;
            }
            ChannelBuffer buf = (ChannelBuffer) msg;

            Object decoded = serializer.read(buf.array());
            if (decoded instanceof Failure) {
                ctx.getChannel().write(decoded);
                return null;
            } else {
                return decoded;
            }
        }
    }

    private static class WesttyProtobufEncoder extends OneToOneEncoder {
        private ProtobufSerializer serializer;

        public WesttyProtobufEncoder(ProtobufSerializer serializer) {
            this.serializer = serializer;
        }

        @Override
        protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg)
                throws Exception {
            if (msg instanceof MessageLite) {

                return wrappedBuffer(serializer.write(msg));
            }
            if (msg instanceof MessageLite.Builder) {
                return wrappedBuffer(((MessageLite.Builder) msg).build().toByteArray());
            }
            return msg;
        }
    }
}
