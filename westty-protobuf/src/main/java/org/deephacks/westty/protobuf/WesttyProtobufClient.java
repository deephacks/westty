package org.deephacks.westty.protobuf;

import static org.jboss.netty.buffer.ChannelBuffers.wrappedBuffer;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.deephacks.westty.protobuf.FailureMessages.Failure;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite;

public class WesttyProtobufClient {
    private ProtobufSerializer serializer;
    private static final Logger log = LoggerFactory.getLogger(WesttyProtobufClient.class);
    private final Lock lock = new ReentrantLock();
    private final Queue<Callback> callbacks = new ConcurrentLinkedQueue<Callback>();
    private final ChannelFactory factory;
    private final ChannelGroup channelGroup = new DefaultChannelGroup("channels");
    private final WesttyProtobufDecoder decoder;
    private final WesttyProtobufEncoder encoder;
    private final LengthFieldPrepender lengthPrepender = new LengthFieldPrepender(2);
    private final ClientHandler clientHandler = new ClientHandler();

    public WesttyProtobufClient(ExecutorService bossExecutor, ExecutorService workerExecutor,
            ProtobufSerializer serializer) {
        this.serializer = serializer;
        this.factory = new NioClientSocketChannelFactory(bossExecutor, workerExecutor);
        this.serializer = serializer;
        this.decoder = new WesttyProtobufDecoder(serializer);
        this.encoder = new WesttyProtobufEncoder();
    }

    public ChannelFuture callAsync(Integer id, Object protoMsg) throws IOException {
        Channel channel = channelGroup.find(id);
        byte[] bytes = serializer.write(protoMsg);
        if (channel == null || !channel.isOpen()) {
            throw new IOException("Channel is not open");
        }
        return channel.write(ChannelBuffers.wrappedBuffer(bytes));
    }

    public Object callSync(Integer id, Object protoMsg) throws IOException, FailureMessageException {
        Channel channel = channelGroup.find(id);
        byte[] bytes = serializer.write(protoMsg);
        Callback callback = new Callback();
        lock.lock();
        try {
            if (channel == null || !channel.isOpen()) {
                throw new IOException("Channel is not open");
            }
            callbacks.add(callback);
            channel.write(ChannelBuffers.wrappedBuffer(bytes));
        } finally {
            lock.unlock();
        }
        Object res = callback.get();
        if (res instanceof Failure) {
            Failure failure = (Failure) res;
            throw new FailureMessageException(failure);
        }
        return res;
    }

    public Integer connect(InetSocketAddress address) throws IOException {
        ClientBootstrap bootstrap = new ClientBootstrap(factory);
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("lengthFrameDecoder", new LengthFieldBasedFrameDecoder(65536, 0,
                        2, 0, 2));
                pipeline.addLast("decoder", decoder);
                pipeline.addLast("lengthPrepender", lengthPrepender);
                pipeline.addLast("encoder", encoder);
                pipeline.addLast("handler", clientHandler);
                return pipeline;
            }
        });

        ChannelFuture future = bootstrap.connect(address);

        if (!future.awaitUninterruptibly().isSuccess()) {
            bootstrap.releaseExternalResources();
            throw new IllegalArgumentException("Could not connect to " + address);
        }

        Channel channel = future.getChannel();

        if (!channel.isConnected()) {
            bootstrap.releaseExternalResources();
            throw new IllegalStateException("Channel could not connect to " + address);
        }
        channelGroup.add(channel);
        return channel.getId();
    }

    public void disconnect(Integer id) {
        Channel channel = channelGroup.find(id);
        if (channel != null && channel.isConnected()) {
            channel.close().awaitUninterruptibly();
        }

    }

    public void shutdown() {
        channelGroup.close().awaitUninterruptibly();
        final class ShutdownNetty extends Thread {
            public void run() {
                factory.releaseExternalResources();
            }
        }
        new ShutdownNetty().start();
    }

    class ClientHandler extends SimpleChannelHandler {

        @Override
        public void handleUpstream(final ChannelHandlerContext ctx, final ChannelEvent e)
                throws Exception {
            if (e instanceof ChannelStateEvent) {
                log.debug(e.toString());
            }
            super.handleUpstream(ctx, e);
        }

        @Override
        public void channelClosed(ChannelHandlerContext ctx, final ChannelStateEvent e)
                throws Exception {
            disconnect(e.getChannel().getId());
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
            callbacks.poll().handle(e.getMessage());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
            final Throwable cause = e.getCause();
            final Channel ch = ctx.getChannel();
            if (cause instanceof ClosedChannelException) {
                log.warn("Attempt to write to closed channel." + ch);

                disconnect(e.getChannel().getId());
            } else if (cause instanceof IOException
                    && "Connection reset by peer".equals(cause.getMessage())) {
                disconnect(e.getChannel().getId());
            } else if (cause instanceof ConnectException
                    && "Connection refused".equals(cause.getMessage())) {
                // server not up, nothing to do 
            } else {
                log.error("Unexpected exception.", e.getCause());
                disconnect(e.getChannel().getId());
            }
        }
    }

    @Sharable
    static class WesttyProtobufDecoder extends OneToOneDecoder {
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
            return serializer.read(buf.array());
        }
    }

    @Sharable
    static class WesttyProtobufEncoder extends OneToOneEncoder {
        @Override
        protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg)
                throws Exception {
            if (msg instanceof MessageLite) {
                return wrappedBuffer(((MessageLite) msg).toByteArray());
            }
            if (msg instanceof MessageLite.Builder) {
                return wrappedBuffer(((MessageLite.Builder) msg).build().toByteArray());
            }
            return msg;
        }
    }

    static class Callback {
        private final CountDownLatch latch = new CountDownLatch(1);
        private Object response;

        Object get() {
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return response;
        }

        void handle(Object response) {
            this.response = response;
            latch.countDown();
        }
    }
}
