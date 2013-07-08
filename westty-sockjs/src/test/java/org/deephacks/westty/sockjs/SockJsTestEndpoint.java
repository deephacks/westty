package org.deephacks.westty.sockjs;


import org.deephacks.westty.cluster.Cluster;
import org.deephacks.westty.config.ServerConfig;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import javax.inject.Inject;
import java.util.LinkedList;

@SockJsEndpoint
public class SockJsTestEndpoint {

    public static final String SERVER_ADDRESS = "server";
    public static final String REPLY_ADDRESS = "reply";
    public static final String CLIENT_ADDRESS = "client";
    public static final LinkedList<JsonObject> messages = new LinkedList<>();

    @Inject
    private ServerConfig config;

    @Inject
    private Cluster cluster;

    @Inject
    private EventBus bus;

    @SockJsMessage(SERVER_ADDRESS)
    public void recieve(Message<JsonObject> message) {
        if(config == null){
            throw new IllegalStateException("Could not inject config into endpoint");
        }

        if(cluster == null) {
            throw new IllegalStateException("Could not inject cluster into endpoint");
        }

        bus.send(CLIENT_ADDRESS, message.body);
    }

    @SockJsMessage(REPLY_ADDRESS)
    public void reply(Message<JsonObject> message) {
        if(config == null){
            throw new IllegalStateException("Could not inject config into endpoint");
        }

        if(cluster == null) {
            throw new IllegalStateException("Could not inject cluster into endpoint");
        }
        message.reply(message.body);
    }

}
