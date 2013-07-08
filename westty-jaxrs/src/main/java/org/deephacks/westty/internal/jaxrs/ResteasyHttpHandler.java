package org.deephacks.westty.internal.jaxrs;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.map.DeserializationConfig;
import org.deephacks.westty.spi.HttpHandler;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpHeaders.Values;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.core.ThreadLocalResteasyProviderFactory;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJacksonProvider;
import org.jboss.resteasy.plugins.server.embedded.SecurityDomain;
import org.jboss.resteasy.plugins.server.netty.NettyHttpRequest;
import org.jboss.resteasy.plugins.server.netty.NettyHttpResponse;
import org.jboss.resteasy.plugins.server.netty.NettySecurityContext;
import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.jboss.resteasy.util.HttpHeaderNames;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.RuntimeDelegate;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Singleton
class ResteasyHttpHandler extends HttpHandler {
    private static final Logger log = LoggerFactory.getLogger(ResteasyHttpHandler.class);
    public static final String JAXRS_CONTEXT_URI = "/jaxrs";

    private ResteasyDeployment deployment;

    /** TODO: fix security */
    protected SecurityDomain domain;

    @Override
    public boolean accept(String uri) {
        return uri.startsWith(JAXRS_CONTEXT_URI);
    }

    @Inject
    public ResteasyHttpHandler(JaxrsApplication jaxrsApps) {
        this.deployment = new ResteasyDeployment();
        deployment.setApplication(jaxrsApps);
        ResteasyJacksonProvider provider = new ResteasyJacksonProvider();
        provider.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ResteasyProviderFactory resteasyFactory = ResteasyProviderFactory.getInstance();
        resteasyFactory.registerProviderInstance(provider);

        deployment.setProviderFactory(resteasyFactory);
        deployment.start();
        log.info("RestEasy started.");
    }

    public void messageReceived(ChannelHandlerContext ctx, MessageEvent event, HttpRequest request)
            throws Exception {

        boolean keepAlive = org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive(request);

        NettyHttpResponse nettyResponse = new NettyHttpResponse(event.getChannel(), keepAlive);

        ResteasyHttpHeaders headers = null;
        ResteasyUriInfo uriInfo = null;
        try {
            headers = NettyUtil.extractHttpHeaders(request);
            uriInfo = NettyUtil.extractUriInfo(request, JaxrsApplication.JAXRS_CONTEXT_URI,
                    "http");
            NettyHttpRequest restEasyRequest = new NettyHttpRequest(headers, uriInfo, request
                    .getMethod().getName(), ((SynchronousDispatcher) deployment.getDispatcher()),
                    nettyResponse,
                    org.jboss.netty.handler.codec.http.HttpHeaders.is100ContinueExpected(request));
            ChannelBufferInputStream is = new ChannelBufferInputStream(request.getContent());
            restEasyRequest.setInputStream(is);
            if (restEasyRequest.is100ContinueExpected()) {
                send100Continue(event);
            }
            try {
                service(restEasyRequest, nettyResponse, true);
            } catch (Failure e1) {
                nettyResponse.reset();
                nettyResponse.setStatus(e1.getErrorCode());
                return;
            } catch (Exception ex) {
                nettyResponse.reset();
                nettyResponse.setStatus(500);
                log.error("Unexpected", ex);
                return;
            }

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

            // Write the response.
            ChannelFuture future = event.getChannel().write(response);

            // Close the non-keep-alive connection after the write operation is done.
            if (!request.isKeepAlive()) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        } catch (Exception e) {
            nettyResponse.sendError(400);
            // made it warn so that people can filter this.
            log.warn("Failed to parse request.", e);
        }
    }

    public void service(org.jboss.resteasy.spi.HttpRequest request,
            org.jboss.resteasy.spi.HttpResponse response, boolean handleNotFound)
            throws IOException {

        try {
            ResteasyProviderFactory defaultInstance = ResteasyProviderFactory.getInstance();
            if (defaultInstance instanceof ThreadLocalResteasyProviderFactory) {
                ThreadLocalResteasyProviderFactory.push(deployment.getProviderFactory());
            }

            SecurityContext securityContext;
            if (domain != null) {
                securityContext = basicAuthentication(request, response);
                if (securityContext == null) // not authenticated
                {
                    return;
                }
            } else {
                securityContext = new NettySecurityContext();
            }
            try {
                ResteasyProviderFactory.pushContext(SecurityContext.class, securityContext);
                if (handleNotFound) {
                    ((SynchronousDispatcher) deployment.getDispatcher()).invoke(request, response);
                } else {
                    ((SynchronousDispatcher) deployment.getDispatcher()).invokePropagateNotFound(
                            request, response);
                }
            } finally {
                ResteasyProviderFactory.clearContextData();
            }
        } finally {
            ResteasyProviderFactory defaultInstance = ResteasyProviderFactory.getInstance();
            if (defaultInstance instanceof ThreadLocalResteasyProviderFactory) {
                ThreadLocalResteasyProviderFactory.pop();
            }

        }
    }

    private SecurityContext basicAuthentication(org.jboss.resteasy.spi.HttpRequest request,
            org.jboss.resteasy.spi.HttpResponse response) throws IOException {
        List<String> headers = request.getHttpHeaders().getRequestHeader(
                HttpHeaderNames.AUTHORIZATION);
        if (!headers.isEmpty()) {
            String auth = headers.get(0);
            if (auth.length() > 5) {
                String type = auth.substring(0, 5);
                type = type.toLowerCase();
                if ("basic".equals(type)) {
                    String cookie = auth.substring(6);
                    cookie = new String(Base64.decodeBase64(cookie.getBytes()));
                    String[] split = cookie.split(":");
                    Principal user = null;
                    try {
                        user = domain.authenticate(split[0], split[1]);
                        return new NettySecurityContext(user, domain, "BASIC", true);
                    } catch (SecurityException e) {
                        response.sendError(HttpResponseCodes.SC_UNAUTHORIZED);
                        return null;
                    }
                } else {
                    response.sendError(HttpResponseCodes.SC_UNAUTHORIZED);
                    return null;
                }
            }
        }
        return null;
    }

    public SecurityDomain getDomain() {
        return domain;
    }

    private void send100Continue(MessageEvent e) {
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, CONTINUE);
        e.getChannel().write(response);
    }

}
