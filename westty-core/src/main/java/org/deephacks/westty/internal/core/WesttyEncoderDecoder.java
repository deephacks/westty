package org.deephacks.westty.internal.core;

import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.RuntimeDelegate;

import org.deephacks.westty.internal.core.WesttyPipelineFactory.HttpRequestType;
import org.deephacks.westty.internal.core.WesttyPipelineFactory.WesttyMessage;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpHeaders.Values;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.logging.Logger;
import org.jboss.resteasy.plugins.server.netty.NettyHttpRequest;
import org.jboss.resteasy.plugins.server.netty.NettyHttpResponse;
import org.jboss.resteasy.specimpl.UriInfoImpl;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.ResteasyDeployment;

public class WesttyEncoderDecoder {
    @Singleton
    public static class WesttyDecoder extends OneToOneDecoder {
        private final Logger logger = Logger.getLogger(WesttyDecoder.class);
        @Inject
        private ResteasyDeployment deployment;
        private final String protocol = "http";

        @Override
        protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg)
                throws Exception {
            if (!(msg instanceof WesttyMessage)) {
                return msg;
            }
            WesttyMessage westtyMessage = (WesttyMessage) msg;
            if (westtyMessage.getRequestType() == HttpRequestType.JAXRS) {
                return handleJaxrsRequest(ctx, channel, msg);
            }
            return msg;
        }

        private Object handleJaxrsRequest(ChannelHandlerContext ctx, Channel channel, Object msg)
                throws Exception {
            org.jboss.netty.handler.codec.http.HttpRequest request = (org.jboss.netty.handler.codec.http.HttpRequest) msg;
            boolean keepAlive = org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive(request);

            NettyHttpResponse response = new NettyHttpResponse(channel, keepAlive);

            HttpHeaders headers = null;
            UriInfoImpl uriInfo = null;
            try {
                headers = NettyUtil.extractHttpHeaders(request);
                uriInfo = NettyUtil.extractUriInfo(request,
                        WesttyJaxrsApplication.JAXRS_CONTEXT_URI, protocol);
                HttpRequest nettyRequest = new NettyHttpRequest(headers, uriInfo, request
                        .getMethod().getName(),
                        ((SynchronousDispatcher) deployment.getDispatcher()), response,
                        org.jboss.netty.handler.codec.http.HttpHeaders
                                .is100ContinueExpected(request));
                ChannelBufferInputStream is = new ChannelBufferInputStream(request.getContent());
                nettyRequest.setInputStream(is);
                return nettyRequest;
            } catch (Exception e) {
                response.sendError(400);
                // made it warn so that people can filter this.
                logger.warn("Failed to parse request.", e);

                return null;
            }
        }

    }

    @Singleton
    public static class WesttyEncoder extends OneToOneEncoder {
        private final Logger logger = Logger.getLogger(WesttyEncoder.class);
        @Inject
        private ResteasyDeployment deployment;

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg)
                throws Exception {
            if (msg instanceof org.jboss.resteasy.spi.HttpResponse) {
                NettyHttpResponse nettyResponse = (NettyHttpResponse) msg;
                // Build the response object.
                HttpResponseStatus status = HttpResponseStatus.valueOf(nettyResponse.getStatus());
                HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);

                for (Map.Entry<String, List<Object>> entry : nettyResponse.getOutputHeaders()
                        .entrySet()) {
                    String key = entry.getKey();
                    for (Object value : entry.getValue()) {
                        RuntimeDelegate.HeaderDelegate delegate = deployment.getProviderFactory()
                                .createHeaderDelegate(value.getClass());
                        if (delegate != null) {
                            response.addHeader(key, delegate.toString(value));
                        } else {
                            response.setHeader(key, value.toString());
                        }
                    }
                }

                nettyResponse.getOutputStream().flush();
                response.setContent(nettyResponse.getBuffer());

                if (nettyResponse.isKeepAlive()) {
                    // Add content length and connection header if needed
                    response.setHeader(Names.CONTENT_LENGTH, response.getContent().readableBytes());
                    response.setHeader(Names.CONNECTION, Values.KEEP_ALIVE);
                }
                return response;
            }
            return msg;

        }

    }
}
