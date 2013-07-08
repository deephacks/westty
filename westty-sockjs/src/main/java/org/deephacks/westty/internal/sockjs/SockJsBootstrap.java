package org.deephacks.westty.internal.sockjs;

import org.deephacks.westty.config.ServerSpecificConfigProxy;
import org.deephacks.westty.config.SockJsConfig;
import org.deephacks.westty.spi.ProviderShutdownEvent;
import org.deephacks.westty.spi.ProviderStartupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSServer;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SockJsBootstrap {

    private Logger log = LoggerFactory.getLogger(SockJsBootstrap.class);
    private HttpServer server;
    private WesttyVertx westtyVertx;
    private EventBus bus;
    private SockJsEndpoints endpoints;
    private SockJsConfig config;

    @Inject
    public SockJsBootstrap(ServerSpecificConfigProxy<SockJsConfig> config, WesttyVertx westtyVertx, EventBus bus, SockJsEndpoints endpoints) {
        this.config = config.get();
        this.bus = bus;
        this.westtyVertx = westtyVertx;
        this.endpoints = endpoints;
    }

    public void startup(@Observes ProviderStartupEvent event) {
        server = westtyVertx.createHttpServer();
        JsonArray permitted = new JsonArray();
        // Let everything through
        permitted.add(new JsonObject());

        SockJSServer sockJSServer = westtyVertx.createSockJSServer(server);
        sockJSServer
                .bridge(new JsonObject().putString("prefix", "/eventbus"), permitted, permitted);
        server.listen(config.getHttpPort());
        log.info("SockJs started on port " + config.getHttpPort());
        endpoints.start(bus);
        log.info("EventBus started on port " + config.getEventBusPort());
    }

    public void shutdown(@Observes ProviderShutdownEvent event) {
        log.info("Shutdown SockJs");
        if (server != null) {
            server.close();
        }
    }

}
