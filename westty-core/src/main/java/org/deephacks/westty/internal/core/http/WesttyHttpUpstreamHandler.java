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
package org.deephacks.westty.internal.core.http;

import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import org.deephacks.westty.internal.core.http.WesttyHttpDecoder.WesttyHttpMessage;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class WesttyHttpUpstreamHandler extends SimpleChannelUpstreamHandler {
    private final static Logger log = LoggerFactory.getLogger(WesttyHttpUpstreamHandler.class);

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
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (e.getMessage() instanceof WesttyHttpMessage) {
            WesttyHttpMessage msg = (WesttyHttpMessage) e.getMessage();
            msg.getHandler().messageReceived(ctx, e, msg);
        }
        super.messageReceived(ctx, e);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        // handle the case of to big requests.
        if (e.getCause() instanceof TooLongFrameException) {
            DefaultHttpResponse response = new DefaultHttpResponse(HTTP_1_1,
                    HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE);
            e.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            e.getCause().printStackTrace();
            e.getChannel().close();
        }
    }

    public ChannelGroup getClientChannels() {
        return clients;
    }
}
