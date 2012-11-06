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

import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.IOException;
import java.io.RandomAccessFile;

import javax.inject.Inject;

import org.deephacks.westty.config.ServerConfig;
import org.deephacks.westty.internal.core.WesttyPipelineFactory.HttpRequestType;
import org.deephacks.westty.internal.core.WesttyPipelineFactory.WesttyMessage;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.DefaultFileRegion;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.jboss.netty.util.CharsetUtil;
import org.jboss.resteasy.logging.Logger;
import org.jboss.resteasy.plugins.server.netty.NettyHttpRequest;
import org.jboss.resteasy.plugins.server.netty.NettyHttpResponse;
import org.jboss.resteasy.plugins.server.netty.RequestHandler;
import org.jboss.resteasy.spi.Failure;

public class WesttyHandler extends SimpleChannelUpstreamHandler {
    private final static Logger logger = Logger.getLogger(RequestHandler.class);

    @Inject
    private RequestDispatcher dispatcher;

    @Inject
    private ServerConfig config;

    private String websocketPath;
    private String staticRootPath;

    private boolean configCached = false;

    private WebSocketServerHandshaker handshaker;

    public WesttyHandler() {
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (!configCached) {
            websocketPath = config.getWebsocket().getUri();
            staticRootPath = config.getWeb().getStaticRoot();
            configCached = true;
        }
        if (e.getMessage() instanceof NettyHttpRequest) {
            NettyHttpRequest request = (NettyHttpRequest) e.getMessage();

            if (request.is100ContinueExpected()) {
                send100Continue(e);
            }
            NettyHttpResponse response = request.getResponse();
            try {
                dispatcher.service(request, response, true);
            } catch (Failure e1) {
                response.reset();
                response.setStatus(e1.getErrorCode());
                return;
            } catch (Exception ex) {
                response.reset();
                response.setStatus(500);
                logger.error("Unexpected", ex);
                return;
            }

            // Write the response.
            ChannelFuture future = e.getChannel().write(response);

            // Close the non-keep-alive connection after the write operation is done.
            if (!request.isKeepAlive()) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        } else if (e.getMessage() instanceof WesttyMessage) {
            WesttyMessage request = (WesttyMessage) e.getMessage();
            if (request.getRequestType() == HttpRequestType.STATIC) {
                handleHttpRequest(ctx, request);
            } else if (request.getRequestType() == HttpRequestType.WEBSOCKET) {
                if ("websocket".equals(request.getHeader("Upgrade"))) {
                    handleWebsocketHandshake(ctx, request);
                }
            }
        } else if (e.getMessage() instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) e.getMessage());
        }

    }

    private void send100Continue(MessageEvent e) {
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, CONTINUE);
        e.getChannel().write(response);
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

    private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest req) throws Exception {

        // Allow only GET methods.
        if (req.getMethod() != HttpMethod.GET) {
            sendHttpResponse(ctx, req, new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.FORBIDDEN));
            return;
        }
        handleStaticContent(ctx, req);
    }

    private static final String READ_ONLY = "r";

    private void handleStaticContent(ChannelHandlerContext ctx, HttpRequest req) throws IOException {
        String uri;
        if ("/".equals(req.getUri())) {
            uri = "/index.html";
        } else {
            uri = req.getUri();
        }
        // File is closed by DefaultFileRegion.releaseExternalResources
        RandomAccessFile file = new RandomAccessFile(staticRootPath + uri, READ_ONLY);
        final DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK);
        HttpHeaders.setContentLength(response, file.length());
        ctx.getChannel().write(response);
        final DefaultFileRegion fileRegion = new DefaultFileRegion(file.getChannel(), 0,
                file.length());
        final ChannelFuture future = ctx.getChannel().write(fileRegion);
        future.addListener(new ChannelFutureListener() {
            public void operationComplete(final ChannelFuture future) {
                fileRegion.releaseExternalResources();
            }
        });
    }

    private void handleWebsocketHandshake(ChannelHandlerContext ctx, HttpRequest req) {
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                this.getWebSocketLocation(req), null, false);
        this.handshaker = wsFactory.newHandshaker(req);
        if (this.handshaker == null) {
            wsFactory.sendUnsupportedWebSocketVersionResponse(ctx.getChannel());
        } else {
            this.handshaker.handshake(ctx.getChannel(), req);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
            this.handshaker.close(ctx.getChannel(), (CloseWebSocketFrame) frame);
            return;
        } else if (frame instanceof PingWebSocketFrame) {
            ctx.getChannel().write(new PongWebSocketFrame(frame.getBinaryData()));
            return;
        } else if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame types not supported",
                    frame.getClass().getName()));
        } else {
            throw new UnsupportedOperationException(frame.toString());
        }

    }

    private void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res) {
        // Generate an error page if response status code is not OK (200).
        if (res.getStatus().getCode() != 200) {
            res.setContent(ChannelBuffers.copiedBuffer(res.getStatus().toString(),
                    CharsetUtil.UTF_8));
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.getChannel().write(res);
        if (!isKeepAlive(req) || res.getStatus().getCode() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private String getWebSocketLocation(HttpRequest req) {
        return "ws://" + req.getHeader(HttpHeaders.Names.HOST) + websocketPath;
    }
}
