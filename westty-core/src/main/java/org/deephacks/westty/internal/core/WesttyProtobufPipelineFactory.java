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
package org.deephacks.westty.internal.core;

import static org.jboss.netty.buffer.ChannelBuffers.wrappedBuffer;

import javax.inject.Inject;

import org.deephacks.westty.protobuf.ProtobufSerializer;
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

import com.google.protobuf.MessageLite;

public class WesttyProtobufPipelineFactory implements ChannelPipelineFactory {
    @Inject
    private WesttyProtobufExtension extension;

    @Inject
    private WesttyProtobufHandler handler;

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        return Channels.pipeline(new LengthFieldBasedFrameDecoder(65536, 0, 2, 0, 2),
                new WesttyProtobufDecoder(extension.getSerializer()), new LengthFieldPrepender(2),
                new WesttyProtobufEncoder(extension.getSerializer()), handler);
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
            return serializer.read(buf.array());
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
