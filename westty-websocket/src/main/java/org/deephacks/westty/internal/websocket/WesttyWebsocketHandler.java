package org.deephacks.westty.internal.websocket;

import javax.inject.Singleton;

import org.deephacks.westty.spi.WesttyHttpHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

@Singleton
public class WesttyWebsocketHandler extends WesttyHttpHandler {
    private WebSocketServerHandshaker handshaker;
    private String websocketPath;

    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (e.getMessage() instanceof WebSocketFrame) {
            WebSocketFrame frame = (WebSocketFrame) e.getMessage();
            // Check for closing frame
            if (frame instanceof CloseWebSocketFrame) {
                this.handshaker.close(ctx.getChannel(), (CloseWebSocketFrame) frame);
                return;
            } else if (frame instanceof PingWebSocketFrame) {
                ctx.getChannel().write(new PongWebSocketFrame(frame.getBinaryData()));
                return;
            } else if (!(frame instanceof TextWebSocketFrame)) {
                throw new UnsupportedOperationException(String.format(
                        "%s frame types not supported", frame.getClass().getName()));
            } else {
                throw new UnsupportedOperationException(frame.toString());
            }
        }
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent event, HttpRequest request)
            throws Exception {
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                this.getWebSocketLocation(request), null, false);
        this.handshaker = wsFactory.newHandshaker(request);
        if (this.handshaker == null) {
            wsFactory.sendUnsupportedWebSocketVersionResponse(ctx.getChannel());
        } else {
            this.handshaker.handshake(ctx.getChannel(), request);
        }
    }

    @Override
    public boolean accept(String uri) {
        return false;
    }

    private String getWebSocketLocation(HttpRequest req) {
        return "ws://" + req.getHeader(HttpHeaders.Names.HOST) + websocketPath;
    }

}
