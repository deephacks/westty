package org.deephacks.westty.sockjs;

import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import org.deephacks.westty.properties.WesttyProperties;
import org.deephacks.westty.properties.WesttyPropertyBuilder;
import org.vertx.java.core.eventbus.impl.DefaultEventBus;

@Alternative
public class WesttySockJsProperties extends WesttyProperties {

    public static final String SOCKJS_EVENTBUS_HOST = "westty.sockjs_eventbus.host";
    public static final String SOCKJS_EVENTBUS_PORT = "westty.sockjs_eventbus.port";

    public static final String SOCKJS_HTTP_HOST = "westty.sockjs_http.host";
    public static final String SOCKJS_HTTP_PORT = "westty.sockjs_http.port";

    @Inject
    public WesttySockJsProperties(WesttyProperties properties) {
        super(properties);
    }

    @WesttyPropertyBuilder(priority = 3000)
    public static void build(WesttyProperties properties) {
        WesttySockJsProperties prop = new WesttySockJsProperties(properties);

        prop.setHttpHost(properties.getPublicIp());
        prop.setHttpPort(8090);

        prop.setEventBusHost(properties.getPrivateIp());
        prop.setEventBusPort(DefaultEventBus.DEFAULT_CLUSTER_PORT);
    }

    public String getHttpHost() {
        return getProperty(SOCKJS_HTTP_HOST);
    }

    public void setHttpHost(String host) {
        setProperty(SOCKJS_HTTP_HOST, host);
    }

    public int getHttpPort() {
        return Integer.parseInt(getProperty(SOCKJS_HTTP_PORT));
    }

    public void setHttpPort(int port) {
        setProperty(SOCKJS_HTTP_PORT, new Integer(port).toString());
    }

    public String getEventBusHost() {
        return getProperty(SOCKJS_EVENTBUS_HOST);
    }

    public void setEventBusHost(String host) {
        setProperty(SOCKJS_EVENTBUS_HOST, host);
    }

    public int getEventBusPort() {
        return Integer.parseInt(getProperty(SOCKJS_EVENTBUS_PORT));
    }

    public void setEventBusPort(int port) {
        setProperty(SOCKJS_EVENTBUS_PORT, new Integer(port).toString());
    }

}
