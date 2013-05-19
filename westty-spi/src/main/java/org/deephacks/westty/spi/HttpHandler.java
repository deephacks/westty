package org.deephacks.westty.spi;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;

public abstract class HttpHandler extends SimpleChannelUpstreamHandler {

    public abstract void messageReceived(ChannelHandlerContext ctx, MessageEvent event,
            HttpRequest request) throws Exception;

    public abstract boolean accept(String uri);
}
