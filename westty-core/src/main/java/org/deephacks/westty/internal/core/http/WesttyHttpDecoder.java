package org.deephacks.westty.internal.core.http;

import java.util.List;

import org.deephacks.westty.properties.WesttyProperties;
import org.deephacks.westty.spi.WesttyHttpHandler;
import org.deephacks.westty.spi.WesttyTemplateCompiler;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpVersion;

class WesttyHttpDecoder extends HttpRequestDecoder {
	private final WesttyProperties properties;
	private final List<WesttyHttpHandler> handlers;
	private final List<WesttyTemplateCompiler> compilers;

	public WesttyHttpDecoder(WesttyProperties properties,
			List<WesttyHttpHandler> handlers,
			List<WesttyTemplateCompiler> compilers) {
		this.handlers = handlers;
		this.properties = properties;
		this.compilers = compilers;
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
			accepted = new WesttyHtmlHandler(properties.getHtmlDir(), compilers);
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

		public WesttyHttpMessage(HttpVersion version, HttpMethod method,
				String uri, WesttyHttpHandler handler) {
			super(version, method, uri);
			this.handler = handler;
		}

		public WesttyHttpHandler getHandler() {
			return handler;
		}
	}
}
