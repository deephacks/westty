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

import static org.jboss.netty.buffer.ChannelBuffers.wrappedBuffer;

import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.deephacks.westty.protobuf.FailureMessages.Failure;
import org.deephacks.westty.protobuf.ProtobufSerializer;
import org.deephacks.westty.protobuf.WesttyProtobufClient;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;

import com.google.protobuf.MessageLite;

@Singleton
class ProtobufPipelineFactory implements ChannelPipelineFactory {
    @Inject
    private ProtobufExtension extension;

    @Inject
    private ProtobufHandler handler;

    @Inject
    private ThreadPoolExecutor executor;

    private ExecutionHandler executionHandler;

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        if (executionHandler == null) {
            this.executionHandler = new ExecutionHandler(executor);
        }
        return Channels.pipeline(new LengthFieldBasedFrameDecoder(
                WesttyProtobufClient.MESSAGE_MAX_SIZE_10MB, 0, WesttyProtobufClient.MESSAGE_LENGTH,
                0, WesttyProtobufClient.MESSAGE_LENGTH),
                new WesttyProtobufDecoder(extension.getSerializer()), new LengthFieldPrepender(
                        WesttyProtobufClient.MESSAGE_LENGTH),
                new WesttyProtobufEncoder(extension.getSerializer()), executionHandler, handler);
    }

    public static class WesttyProtobufDecoder extends OneToOneDecoder {
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
