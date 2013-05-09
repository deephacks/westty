package org.deephacks.westty.internal.sockjs;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.deephacks.westty.sockjs.WesttySockJsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSServer;

@Singleton
public class EventBusProducer {
    private static final Logger log = LoggerFactory.getLogger(EventBusProducer.class);
    private final WesttySockJsProperties props;
    private HttpServer server;
    private WesttyVertx westtyVertx;
    private EventBus bus;

    @Inject
    public EventBusProducer(WesttyVertx westtyVertx) {
        this.props = new WesttySockJsProperties();
        this.westtyVertx = westtyVertx;
        server = westtyVertx.createHttpServer();
        JsonArray permitted = new JsonArray();
        // Let everything through
        permitted.add(new JsonObject());

        SockJSServer sockJSServer = westtyVertx.createSockJSServer(server);
        sockJSServer
                .bridge(new JsonObject().putString("prefix", "/eventbus"), permitted, permitted);
        server.listen(props.getHttpPort());
        log.info("SockJs started on " + props.getHttpHost() + ":" + props.getHttpPort());
        bus = westtyVertx.eventBus();
        SockJsEnpointBootstrap.start(bus);
        log.info("EventBus started on " + props.getEventBusHost() + ":" + props.getEventBusPort());
    }

    @Produces
    @Singleton
    public EventBus createEventBus() {
        return bus;
    }

    public void shutdown(@Observes BeforeShutdown event) {
        if (server != null) {
            server.close();
        }
    }

}
