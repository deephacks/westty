package org.deephacks.westty.internal.jaxrs;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.jboss.resteasy.util.HttpHeaderNames;
import org.jboss.resteasy.util.MediaTypeHelper;
import org.jboss.resteasy.util.PathHelper;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class NettyUtil
{
    public static ResteasyUriInfo extractUriInfo(HttpRequest request, String contextPath, String protocol)
    {
        String host = HttpHeaders.getHost(request, "unknown");
        String uri = request.getUri();

        URI absoluteURI = URI.create(protocol + "://" + host + uri);

        String path = PathHelper.getEncodedPathInfo(absoluteURI.getRawPath(), contextPath);
        if (!path.startsWith("/"))
        {
            path = "/" + path;
        }

        URI baseURI = absoluteURI;
        if (!path.trim().equals(""))
        {
            String tmpContextPath = contextPath;
            if (!tmpContextPath.endsWith("/")) tmpContextPath += "/";
            baseURI = UriBuilder.fromUri(absoluteURI).replacePath(tmpContextPath).replaceQuery(null).build();
        }
        else
        {
            baseURI = UriBuilder.fromUri(absoluteURI).replaceQuery(null).build();
        }
        URI relativeURI = UriBuilder.fromUri(path).replaceQuery(absoluteURI.getRawQuery()).build();
        //System.out.println("path: " + path);
        //System.out.println("query string: " + request.getQueryString());
        ResteasyUriInfo uriInfo = new ResteasyUriInfo(baseURI, relativeURI);
        return uriInfo;
    }

    public static ResteasyHttpHeaders extractHttpHeaders(HttpRequest request)
    {

        MultivaluedMap<String, String> requestHeaders = extractRequestHeaders(request);
        ResteasyHttpHeaders headers = new ResteasyHttpHeaders(requestHeaders);

        Map<String, Cookie> cookies = extractCookies(requestHeaders);
        headers.setCookies(cookies);
        // test parsing should throw an exception on error
        headers.testParsing();
        return headers;

    }

    static Map<String, Cookie> extractCookies(MultivaluedMap<String, String> headers)
    {
        Map<String, Cookie> cookies = new HashMap<String, Cookie>();
        List<String> cookieHeaders = headers.get("Cookie");
        if (cookieHeaders == null) return cookies;

        for (String cookieVal : cookieHeaders)
        {
            Cookie cookie = Cookie.valueOf(cookieVal);
            cookies.put(cookie.getName(), cookie);
        }
        return cookies;
    }

    public static List<MediaType> extractAccepts(MultivaluedMap<String, String> requestHeaders)
    {
        List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
        List<String> accepts = requestHeaders.get(HttpHeaderNames.ACCEPT);
        if (accepts == null) return acceptableMediaTypes;

        for (String accept : accepts)
        {
            acceptableMediaTypes.addAll(MediaTypeHelper.parseHeader(accept));
        }
        return acceptableMediaTypes;
    }

    public static List<String> extractLanguages(MultivaluedMap<String, String> requestHeaders)
    {
        List<String> acceptable = new ArrayList<String>();
        List<String> accepts = requestHeaders.get(HttpHeaderNames.ACCEPT_LANGUAGE);
        if (accepts == null) return acceptable;

        for (String accept : accepts)
        {
            String[] splits = accept.split(",");
            for (String split : splits) acceptable.add(split.trim());
        }
        return acceptable;
    }

    public static MultivaluedMap<String, String> extractRequestHeaders(HttpRequest request)
    {
        Headers<String> requestHeaders = new Headers<String>();

        for (Map.Entry<String, String> header : request.getHeaders())
        {
            requestHeaders.add(header.getKey(), header.getValue());
        }
        return requestHeaders;
    }
}
