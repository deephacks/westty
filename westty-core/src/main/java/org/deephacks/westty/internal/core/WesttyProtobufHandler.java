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

import java.io.IOException;
import java.net.ConnectException;
import java.nio.channels.ClosedChannelException;

import javax.inject.Inject;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class WesttyProtobufHandler extends SimpleChannelHandler {
    @Inject
    private WesttyProtobufExtension extension;

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
        extension.invokeEndpoint(e.getMessage());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        final Throwable cause = e.getCause();
        final Channel ch = ctx.getChannel();
        cause.printStackTrace();
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
