package org.deephacks.westty.sockjs;

import org.deephacks.westty.cluster.Cluster;
import org.deephacks.westty.config.ServerConfig;
import org.deephacks.westty.server.Server;
import org.deephacks.westty.test.WesttyJUnit4Runner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;


@SockJsEndpoint
@RunWith(WesttyJUnit4Runner.class)
public class SockJsTest {

    @Inject
    private EventBus bus;

    @Inject
    private Cluster cluster;

    private static final LinkedList<JsonObject> messages = new LinkedList<>();

    /**
     * Test that the server can recieve messages and that it can use
     * the eventbus to send messages.
     */
    @Test
    public void test_new_message_reply() throws Exception {
        String text = UUID.randomUUID().toString();
        JsonObject json = new JsonObject();
        json.putString("text", text);
        bus.send(SockJsTestEndpoint.SERVER_ADDRESS, json);
        Thread.sleep(500);
        JsonObject message = messages.poll();
        assertThat(message.getString("text"), is(text));
    }

    /**
     * Test that the server can make a direct reply to incoming messages.
     */
    @Test
    public void test_message_reply() throws Exception {
        String text = UUID.randomUUID().toString();
        JsonObject json = new JsonObject();
        json.putString("text", text);
        final CountDownLatch latch = new CountDownLatch(1);

        bus.send(SockJsTestEndpoint.REPLY_ADDRESS, json, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> event) {
                messages.addFirst(event.body);
                latch.countDown();
            }
        });
        latch.await();
        JsonObject message = messages.poll();
        assertThat(message.getString("text"), is(text));
    }

    /**
     * Test that we can inject the cluster and that it gives a correct view of
     * the cluster membership.
     */
    @Test
    public void test_cluster_members(){
        assertNotNull(cluster);
        assertThat(cluster.getMembers().size(), is(1));
        Server server = cluster.getMembers().iterator().next();
        assertNotNull(server);
        assertThat(server.getPort(), is(ServerConfig.DEFAULT_CLUSTER_PORT));
        assertThat(server.getHost(), is(ServerConfig.DEFAULT_IP_ADDRESS));
    }

    @SockJsMessage(SockJsTestEndpoint.CLIENT_ADDRESS)
    public void recieve(Message<JsonObject> message) {
        messages.addFirst(message.body);
    }

}
