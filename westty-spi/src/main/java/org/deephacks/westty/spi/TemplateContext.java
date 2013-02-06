package org.deephacks.westty.spi;

import java.io.Reader;
import java.io.Writer;

public class TemplateContext {
	private final Writer writer;
	private final Reader reader;
	private final String uri;

	public TemplateContext(Writer writer, Reader reader, String uri) {
		this.writer = writer;
		this.reader = reader;
		this.uri = uri;

	}

	public String getUri() {
		return uri;
	}

	public Writer getWriter() {
		return writer;
	}

	public Reader getReader() {
		return reader;
	}
}
