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
package org.deephacks.westty.protobuf;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.channels.ClosedChannelException;

import javax.inject.Inject;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WesttyProtobufHandler extends SimpleChannelHandler {
    @Inject
    private WesttyProtobufExtension extension;
    private Logger log = LoggerFactory.getLogger(WesttyProtobufHandler.class);

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
        Object res = extension.invokeEndpoint(e.getMessage());
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
}
