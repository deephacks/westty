package org.deephacks.westty.protobuf;

import static org.jboss.netty.buffer.ChannelBuffers.wrappedBuffer;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
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

import com.google.protobuf.MessageLite;

public class ProtobufRpcClient {
    private final InetSocketAddress address;
    private ClientBootstrap bootstrap;
    private Channel channel;
    private ProtobufSerializer serializer;

    public ProtobufRpcClient(final InetSocketAddress address, ProtobufSerializer serializer) {
        this.address = address;
        this.serializer = serializer;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public Channel getChannel() {
        return channel;
    }

    public void write(Object protoMsg) throws IOException {
        byte[] bytes = serializer.write(protoMsg);
        channel.write(ChannelBuffers.wrappedBuffer(bytes));
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

        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
            final Throwable cause = e.getCause();
            final Channel ch = ctx.getChannel();
            if (cause instanceof ClosedChannelException) {

            } else if (cause instanceof IOException
                    && "Connection reset by peer".equals(cause.getMessage())) {

            } else if (cause instanceof ConnectException
                    && "Connection refused".equals(cause.getMessage())) {
                // server not up, nothing to do 
            } else {
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

}
