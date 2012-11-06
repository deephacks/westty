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

import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.ext.RuntimeDelegate;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpHeaders.Values;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.jboss.resteasy.plugins.server.netty.NettyHttpResponse;
import org.jboss.resteasy.spi.ResteasyDeployment;

public class WesttyEncoder extends OneToOneEncoder {
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
