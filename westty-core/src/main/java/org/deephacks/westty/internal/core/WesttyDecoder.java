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

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;

import org.deephacks.westty.internal.core.WesttyPipelineFactory.HttpRequestType;
import org.deephacks.westty.internal.core.WesttyPipelineFactory.WesttyMessage;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.logging.Logger;
import org.jboss.resteasy.plugins.server.netty.NettyHttpRequest;
import org.jboss.resteasy.plugins.server.netty.NettyHttpResponse;
import org.jboss.resteasy.specimpl.UriInfoImpl;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.ResteasyDeployment;

public class WesttyDecoder extends OneToOneDecoder {

    private final static Logger logger = Logger.getLogger(WesttyDecoder.class);
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
            uriInfo = NettyUtil.extractUriInfo(request, WesttyJaxrsApplication.JAXRS_CONTEXT_URI,
                    protocol);
            HttpRequest nettyRequest = new NettyHttpRequest(headers, uriInfo, request.getMethod()
                    .getName(), ((SynchronousDispatcher) deployment.getDispatcher()), response,
                    org.jboss.netty.handler.codec.http.HttpHeaders.is100ContinueExpected(request));
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
