package org.deephacks.westty.internal.core.http;

import org.deephacks.westty.spi.HttpHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpVersion;

import java.io.File;
import java.util.List;

class HttpDecoder extends HttpRequestDecoder {
    private final List<HttpHandler> handlers;
    private File htmlDir;
    public HttpDecoder(File htmlDir, List<HttpHandler> handlers) {
        this.handlers = handlers;
        this.htmlDir = htmlDir;
    }

    @Override
    protected HttpMessage createMessage(String[] initialLine) throws Exception {
        String uri = initialLine[1];
        HttpHandler accepted = null;
        for (HttpHandler handler : handlers) {
            if (handler.accept(uri)) {
                accepted = handler;
                break;
            }
        }
        if (accepted == null) {
            accepted = new HtmlHandler(htmlDir);
        }
        return new WesttyHttpMessage(HttpVersion.valueOf(initialLine[2]),
                HttpMethod.valueOf(initialLine[0]), initialLine[1], accepted);
    }

    @Override
    protected boolean isDecodingRequest() {
        return true;
    }

    public final static class WesttyHttpMessage extends DefaultHttpRequest {
        private HttpHandler handler;

        public WesttyHttpMessage(HttpVersion version, HttpMethod method, String uri,
                HttpHandler handler) {
            super(version, method, uri);
            this.handler = handler;
        }

        public HttpHandler getHandler() {
            return handler;
        }
    }
}
