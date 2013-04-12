package org.deephacks.westty.internal.sockjs;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.deephacks.westty.properties.WesttyProperties;
import org.deephacks.westty.sockjs.WesttySockJsProperties;
import org.deephacks.westty.spi.WesttyModule;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSServer;

@Singleton
public class WesttySockJsModule implements WesttyModule {
    private final WesttySockJsProperties props;
    private HttpServer server;
    private WesttyVertx westtyVertx;
    private EventBus bus;

    @Inject
    public WesttySockJsModule(WesttyProperties properties, WesttyVertx westtyVertx) {
        this.props = new WesttySockJsProperties(properties);
        this.westtyVertx = westtyVertx;
    }

    @Produces
    @Singleton
    public EventBus createEventBus() {
        return bus;
    }

    @Override
    public int priority() {
        return 600;
    }

    @Override
    public synchronized void startup() {
        server = westtyVertx.createHttpServer();
        JsonArray permitted = new JsonArray();
        // Let everything through
        permitted.add(new JsonObject());

        SockJSServer sockJSServer = westtyVertx.createSockJSServer(server);
        sockJSServer
                .bridge(new JsonObject().putString("prefix", "/eventbus"), permitted, permitted);
        server.listen(props.getHttpPort());

        bus = westtyVertx.eventBus();
        WesttySockJsBootstrap.start(bus);
    }

    @Override
    public void shutdown() {
        if (server != null) {
            server.close();
        }
    }

}
