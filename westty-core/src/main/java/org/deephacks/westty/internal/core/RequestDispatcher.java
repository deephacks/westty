package org.deephacks.westty.internal.core;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.codec.binary.Base64;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.core.ThreadLocalResteasyProviderFactory;
import org.jboss.resteasy.plugins.server.embedded.SecurityDomain;
import org.jboss.resteasy.plugins.server.netty.NettySecurityContext;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.util.HttpHeaderNames;
import org.jboss.resteasy.util.HttpResponseCodes;

public class RequestDispatcher {
    /** TODO: fix security */
    protected SecurityDomain domain;
    @Inject
    private ResteasyDeployment deployment;

    public RequestDispatcher() {
    }

    public SecurityDomain getDomain() {
        return domain;
    }

    public void service(HttpRequest request, HttpResponse response, boolean handleNotFound)
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

    private SecurityContext basicAuthentication(HttpRequest request, HttpResponse response)
            throws IOException {
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

}