package org.deephacks.westty.internal.core.http;

import org.deephacks.westty.WesttyProperties;
import org.deephacks.westty.spi.WesttyHttpHandler;
import org.jboss.netty.handler.codec.http.*;

import java.util.List;

class HttpDecoder extends HttpRequestDecoder {
    private final List<WesttyHttpHandler> handlers;

    public HttpDecoder(List<WesttyHttpHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    protected HttpMessage createMessage(String[] initialLine) throws Exception {
        String uri = initialLine[1];
        WesttyHttpHandler accepted = null;
        for (WesttyHttpHandler handler : handlers) {
            if (handler.accept(uri)) {
                accepted = handler;
                break;
            }
        }
        if (accepted == null) {
            accepted = new HtmlHandler(WesttyProperties.getHtmlDir());
        }
        return new WesttyHttpMessage(HttpVersion.valueOf(initialLine[2]),
                HttpMethod.valueOf(initialLine[0]), initialLine[1], accepted);
    }

    @Override
    protected boolean isDecodingRequest() {
        return true;
    }

    public final static class WesttyHttpMessage extends DefaultHttpRequest {
        private WesttyHttpHandler handler;

        public WesttyHttpMessage(HttpVersion version, HttpMethod method, String uri,
                WesttyHttpHandler handler) {
            super(version, method, uri);
            this.handler = handler;
        }

        public WesttyHttpHandler getHandler() {
            return handler;
        }
    }
}
