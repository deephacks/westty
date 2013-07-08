package org.deephacks.westty.internal.sockjs;

import org.vertx.java.core.eventbus.EventBus;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

public class SockJsProducer {
    @Inject
    private SockJsEnpointExtension extension;

    @Inject
    private WesttyVertx vertx;

    @Produces
    @Singleton
    public SockJsEndpoints produceEndpoints(){
        return extension.getEndpoints();
    }

    @Produces
    @Singleton
    public EventBus produceEventBus(){
        return vertx.eventBus();
    }
}
