package org.deephacks.westty.internal.core;

import static org.jboss.netty.channel.Channels.pipeline;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.net.ssl.SSLEngine;

import org.deephacks.tools4j.config.RuntimeContext;
import org.deephacks.westty.config.ServerConfig;
import org.deephacks.westty.config.WesttyApplication;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jboss.netty.handler.ssl.SslHandler;

public class WesttySecurePipelineFactory implements ChannelPipelineFactory {

    @Inject
    private ServerConfig server;

    @Inject
    private WesttyEncoder encoder;

    @Inject
    private WesttyDecoder decoder;

    @Inject
    private ServerConfig config;
    private String staticPath;
    private String jaxrsPath;

    @Inject
    private WesttyHandler requestHandler;

    @Inject
    private RuntimeContext ctx;
    @Inject
    private WesttySslContextFactory sslContextFactory;

    private ExecutionHandler executionHandler;

    private SSLEngine engine;

    public ChannelPipeline getPipeline() throws Exception {
        if (engine == null) {
            sslContextFactory.init();
            engine = sslContextFactory.getServerContext().createSSLEngine();
            engine.setUseClientMode(false);
        }
        staticPath = config.getWeb().getUri();
        jaxrsPath = config.getJaxrs().getUri();
        if (executionHandler == null) {
            if (config.getExecutorThreadCount() > 0) {
                this.executionHandler = new ExecutionHandler(
                        new OrderedMemoryAwareThreadPoolExecutor(config.getExecutorThreadCount(),
                                0L, 0L));
            }
        }
        ChannelPipeline pipeline = pipeline();
        pipeline.addLast("ssl", new SslHandler(engine));
        pipeline.addLast("decoder", new HttpDetector());
        pipeline.addLast("westtyDecoder", decoder);
        pipeline.addLast("aggregator",
                new HttpChunkAggregator(server.getMaxHttpContentChunkLength()));
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("westtyEncoder", encoder);
        pipeline.addLast("handshake", new WesttySecureHandler());
        if (executionHandler != null) {
            pipeline.addLast("executionHandler", executionHandler);
        }
        pipeline.addLast("handler", requestHandler);

        return pipeline;
    }

    final class HttpDetector extends HttpRequestDecoder {

        private ConcurrentHashMap<String, WesttyApplication> applications;

        @Override
        protected HttpMessage createMessage(String[] initialLine) throws Exception {
            if (applications == null) {
                applications = getApplications();
            }
            String uri = initialLine[1];
            HttpRequestType requestType = null;
            if (uri.startsWith(jaxrsPath)) {
                requestType = HttpRequestType.JAXRS;
            } else if (uri.startsWith(staticPath)) {
                requestType = HttpRequestType.STATIC;
            }

            return new WesttyMessage(HttpVersion.valueOf(initialLine[2]),
                    HttpMethod.valueOf(initialLine[0]), initialLine[1], requestType);
        }

        private ConcurrentHashMap<String, WesttyApplication> getApplications() {
            ConcurrentHashMap<String, WesttyApplication> result = new ConcurrentHashMap<String, WesttyApplication>();
            List<WesttyApplication> apps = ctx.all(WesttyApplication.class);
            for (WesttyApplication app : apps) {
                result.put(app.getAppUri(), app);
            }
            return result;
        }

        @Override
        protected boolean isDecodingRequest() {
            return true;
        }
    }

    final static class WesttyMessage extends DefaultHttpRequest {
        private HttpRequestType requestType;

        public WesttyMessage(HttpVersion version, HttpMethod method, String uri,
                HttpRequestType requestType) {
            super(version, method, uri);
            this.requestType = requestType;
        }

        public HttpRequestType getRequestType() {
            return requestType;
        }
    }

    static enum HttpRequestType {
        WEBSOCKET, STATIC, JAXRS
    }
}
