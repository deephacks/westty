package org.deephacks.westty.internal.core.https;


public class WesttySecurePipelineFactory {// implements ChannelPipelineFactory {

    //    @Inject
    //    private ServerConfig server;
    //
    //    @Inject
    //    private WesttyEncoder encoder;
    //
    //    @Inject
    //    private WesttyDecoder decoder;
    //
    //    @Inject
    //    private ServerConfig config;
    //    private String staticPath;
    //    private String jaxrsPath;
    //
    //    @Inject
    //    private WesttyHttpDispatcher requestHandler;
    //
    //    @Inject
    //    private ThreadPoolExecutor executor;
    //
    //    @Inject
    //    private RuntimeContext ctx;
    //    @Inject
    //    private WesttySslContextFactory sslContextFactory;
    //
    //    private ExecutionHandler executionHandler;
    //
    //    private SSLEngine engine;
    //
    //    public ChannelPipeline getPipeline() throws Exception {
    //        if (engine == null) {
    //            sslContextFactory.init();
    //            engine = sslContextFactory.getServerContext().createSSLEngine();
    //            engine.setUseClientMode(false);
    //        }
    //        staticPath = config.getWeb().getUri();
    //        jaxrsPath = config.getJaxrs().getUri();
    //        if (executionHandler == null) {
    //            this.executionHandler = new ExecutionHandler(executor);
    //        }
    //        ChannelPipeline pipeline = pipeline();
    //        pipeline.addLast("ssl", new SslHandler(engine));
    //        pipeline.addLast("decoder", new HttpDetector());
    //        pipeline.addLast("westtyDecoder", decoder);
    //        pipeline.addLast("aggregator",
    //                new HttpChunkAggregator(server.getMaxHttpContentChunkLength()));
    //        pipeline.addLast("encoder", new HttpResponseEncoder());
    //        pipeline.addLast("westtyEncoder", encoder);
    //        pipeline.addLast("handshake", new WesttySecureHandler());
    //        if (executionHandler != null) {
    //            pipeline.addLast("executionHandler", executionHandler);
    //        }
    //        pipeline.addLast("handler", requestHandler);
    //
    //        return pipeline;
    //    }
    //
    //    final class HttpDetector extends HttpRequestDecoder {
    //
    //        private ConcurrentHashMap<String, WesttyApplication> applications;
    //
    //        @Override
    //        protected HttpMessage createMessage(String[] initialLine) throws Exception {
    //            if (applications == null) {
    //                applications = getApplications();
    //            }
    //            String uri = initialLine[1];
    //            HttpRequestType requestType = null;
    //            if (uri.startsWith(jaxrsPath)) {
    //                requestType = HttpRequestType.JAXRS;
    //            } else if (uri.startsWith(staticPath)) {
    //                requestType = HttpRequestType.STATIC;
    //            }
    //
    //            return new WesttyMessage(HttpVersion.valueOf(initialLine[2]),
    //                    HttpMethod.valueOf(initialLine[0]), initialLine[1], requestType);
    //        }
    //
    //        private ConcurrentHashMap<String, WesttyApplication> getApplications() {
    //            ConcurrentHashMap<String, WesttyApplication> result = new ConcurrentHashMap<String, WesttyApplication>();
    //            List<WesttyApplication> apps = ctx.all(WesttyApplication.class);
    //            for (WesttyApplication app : apps) {
    //                result.put(app.getAppUri(), app);
    //            }
    //            return result;
    //        }
    //
    //        @Override
    //        protected boolean isDecodingRequest() {
    //            return true;
    //        }
    //    }
    //
    //    final static class WesttyMessage extends DefaultHttpRequest {
    //        private HttpRequestType requestType;
    //
    //        public WesttyMessage(HttpVersion version, HttpMethod method, String uri,
    //                HttpRequestType requestType) {
    //            super(version, method, uri);
    //            this.requestType = requestType;
    //        }
    //
    //        public HttpRequestType getRequestType() {
    //            return requestType;
    //        }
    //    }
    //
    //    static enum HttpRequestType {
    //        WEBSOCKET, STATIC, JAXRS
    //    }
}
