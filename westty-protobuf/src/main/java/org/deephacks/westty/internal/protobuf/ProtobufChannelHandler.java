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

import org.deephacks.westty.internal.protobuf.ProtobufEndpoints.ProtobufEndpointProxy;
import org.deephacks.westty.protobuf.FailureMessages.Failure;
import org.deephacks.westty.protobuf.ProtobufException.FailureCode;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.channels.ClosedChannelException;

class ProtobufChannelHandler extends SimpleChannelHandler {
    private static final Logger log = LoggerFactory.getLogger(ProtobufChannelHandler.class);

    @Inject
    private ProtobufEndpoints endpoints;

    @Inject
    private BeanManager beanManager;

    private ChannelGroup clients = new DefaultChannelGroup("connected-clients");

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        clients.add(ctx.getChannel());
        super.channelConnected(ctx, e);
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        clients.remove(ctx.getChannel());
        super.channelClosed(ctx, e);
    }

    @Override
    public void handleUpstream(final ChannelHandlerContext ctx, final ChannelEvent e)
            throws Exception {
        if (e instanceof ChannelStateEvent) {
            log.debug(e.toString());
        }
        super.handleUpstream(ctx, e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        Object protoMsg = e.getMessage();
        ProtobufEndpointProxy proxy = endpoints.get(protoMsg);
        Object res = null;
        if (proxy != null) {
            res = proxy.invoke(protoMsg);
        } else {
            res = Failure.newBuilder().setCode(FailureCode.NOT_IMPLEMENTED.getCode()).setMsg("")
                    .build();
        }
        if (res != null && e.getChannel().isConnected()) {
            e.getChannel().write(res);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        final Throwable cause = e.getCause();
        final Channel ch = ctx.getChannel();
        if (cause instanceof ClosedChannelException) {
            log.warn("Attempt to write to closed channel " + ch);
        } else if (cause instanceof IOException
                && "Connection reset by peer".equals(cause.getMessage())) {
            // a client may have disconnected
        } else if (cause instanceof ConnectException
                && "Connection refused".equals(cause.getMessage())) {
            // server not up, nothing to do
        } else {
            log.error("Unexpected exception downstream for " + ch, cause);
            e.getChannel().close();
        }
    }

    public ChannelGroup getClientChannels() {
        return clients;
    }
}
