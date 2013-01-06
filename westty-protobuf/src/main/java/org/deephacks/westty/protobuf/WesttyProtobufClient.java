package org.deephacks.westty.protobuf;

import static org.jboss.netty.buffer.ChannelBuffers.wrappedBuffer;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite;

public class WesttyProtobufClient {
    private final InetSocketAddress address;
    private ClientBootstrap bootstrap;
    private Channel channel;
    private ProtobufSerializer serializer;
    private Logger log = LoggerFactory.getLogger(WesttyProtobufClient.class);
    private final Lock lock = new ReentrantLock();
    private final Queue<Callback> callbacks = new ConcurrentLinkedQueue<Callback>();

    public WesttyProtobufClient(final InetSocketAddress address, ProtobufSerializer serializer) {
        this.address = address;
        this.serializer = serializer;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public Channel getChannel() {
        return channel;
    }

    public ChannelFuture callAsync(Object protoMsg) throws IOException {
        byte[] bytes = serializer.write(protoMsg);
        if (!channel.isOpen()) {
            throw new IOException("Channel is not open");
        }
        return channel.write(ChannelBuffers.wrappedBuffer(bytes));
    }

    public Object callSync(Object protoMsg) throws IOException {
        byte[] bytes = serializer.write(protoMsg);
        Callback callback = new Callback();
        lock.lock();
        try {
            callbacks.add(callback);
            if (!channel.isOpen()) {
                throw new IOException("Channel is not open");
            }
            channel.write(ChannelBuffers.wrappedBuffer(bytes));
        } finally {
            lock.unlock();
        }
        return callback.get();
    }

    public void connect() throws IOException {
        ChannelFactory factory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool());
        this.bootstrap = new ClientBootstrap(factory);
        this.bootstrap.setPipelineFactory(new WesttyProtobufPipelineFactory());

        ChannelFuture future = bootstrap.connect(address);
        if (!future.awaitUninterruptibly().isSuccess()) {
            bootstrap.releaseExternalResources();
            throw new IllegalArgumentException("Could not connect to " + address);
        }
        this.channel = future.getChannel();
        if (!channel.isConnected()) {
            bootstrap.releaseExternalResources();
            throw new IllegalStateException("Channel could not connect to " + address);
        }

    }

    public void disconnect() {
        if (channel != null && channel.isConnected()) {
            channel.close().awaitUninterruptibly();
        }
        final class ShutdownNetty extends Thread {
            public void run() {
                bootstrap.releaseExternalResources();
            }
        }
        new ShutdownNetty().start();
    }

    /**
     * Is client still connected to the RpcServer. 
     */
    public boolean isConnected() {
        if (channel == null || !channel.isOpen() || !channel.isConnected()) {
            return false;
        }
        return true;
    }

    public class ClientHandler extends SimpleChannelHandler {

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
            // Netty does not want to be shutdown from within a NIO thread.
            final class ShutdownNetty extends Thread {
                public void run() {
                    e.getChannel().getFactory().releaseExternalResources();
                }
            }
            new ShutdownNetty().start();

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
                disconnect();
            } else if (cause instanceof IOException
                    && "Connection reset by peer".equals(cause.getMessage())) {
                disconnect();
            } else if (cause instanceof ConnectException
                    && "Connection refused".equals(cause.getMessage())) {
                // server not up, nothing to do 
            } else {
                log.error("Unexpected exception.", e.getCause());
                disconnect();
            }
        }
    }

    public class WesttyProtobufPipelineFactory implements ChannelPipelineFactory {

        @Override
        public ChannelPipeline getPipeline() throws Exception {
            return Channels.pipeline(new LengthFieldBasedFrameDecoder(65536, 0, 2, 0, 2),
                    new WesttyProtobufDecoder(serializer), new LengthFieldPrepender(2),
                    new WesttyProtobufEncoder(), new ClientHandler());
        }

        public class WesttyProtobufDecoder extends OneToOneDecoder {
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

        private class WesttyProtobufEncoder extends OneToOneEncoder {
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
