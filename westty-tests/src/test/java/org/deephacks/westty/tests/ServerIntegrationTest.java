package org.deephacks.westty.tests;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.deephacks.westty.datasource.DataSourceProperties;
import org.deephacks.westty.jaxrs.JaxrsConfigClient;
import org.deephacks.westty.protobuf.ProtobufClient;
import org.deephacks.westty.protobuf.ProtobufSerializer;
import org.deephacks.westty.spi.IoExecutors;
import org.deephacks.westty.test.SQLExec;
import org.deephacks.westty.test.TestBootstrap;
import org.deephacks.westty.test.WesttyJUnit4Runner;
import org.deephacks.westty.tests.JsonEntity.Protocol;
import org.deephacks.westty.tests.ServerMessages.CreateRequest;
import org.deephacks.westty.tests.ServerMessages.JsonMessage;
import org.deephacks.westty.tests.ServerMessages.ListRequest;
import org.deephacks.westty.tests.ServerMessages.ListResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.hasItem;

@RunWith(WesttyJUnit4Runner.class)
public class ServerIntegrationTest {
    private static final ProtobufSerializer serializer = new ProtobufSerializer();
    private static final String TABLE = "CREATE TABLE JSON (ID varchar(255), JSON varchar(1024), PROTOCOL varchar(255));";
    private static final ProtobufClient protobuf = new ProtobufClient(new IoExecutors(), serializer);
    private static final JaxrsConfigClient config = new JaxrsConfigClient();
    private static final JaxrsClient jaxrs = new JaxrsClient();
    @Inject
    private EventBus bus;

    @TestBootstrap
    public static void bootstrap() throws Exception {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        serializer.registerResource("META-INF/server.desc");
        DataSourceProperties ds = new DataSourceProperties();
        SQLExec sql = new SQLExec(ds.getUsername(), ds.getPassword(), ds.getUrl());
        sql.execute(TABLE);
        sql.executeResource("META-INF/install_config_derby.ddl", false);
    }

    /**
     * Test that jpa can be used from a jaxrs endpoint.
     */
    @Test
    public void test_jaxrs_and_jpa() {
        JsonEntity entity = new JsonEntity(UUID.randomUUID().toString(), Protocol.JAXRS, "test");
        jaxrs.posthttp(ServerEndpoint.JAXRS_PATH + "/create", JsonUtil.toJson(entity));
        List<JsonEntity> list = JsonUtil.fromJsonList(jaxrs.gethttp(ServerEndpoint.JAXRS_PATH + "/list"));
        assertThat(list, hasItem(entity));
    }
    /**
     * Test that multiple jpa transactionl methods can
     * be executed and rollbacked as one transaction.
     */
    @Test
    public void test_nested_jpa_transaction(){
        JsonEntity entity = new JsonEntity(UUID.randomUUID().toString(), Protocol.JAXRS, "test");
        jaxrs.posthttp(ServerEndpoint.JAXRS_PATH + "/nested", JsonUtil.toJson(entity));
        List<JsonEntity> list = JsonUtil.fromJsonList(jaxrs.gethttp(ServerEndpoint.JAXRS_PATH + "/list"));
        assertThat(list, not(hasItem(entity)));
    }
    /**
     * Test that jpa can be used from a protobuf endpoint.
     */
    @Test
    public void test_protobuf_and_jpa() throws Exception {
        String id = UUID.randomUUID().toString();
        JsonMessage msg = JsonMessage.newBuilder().setId(id).setJson("msg").build();
        CreateRequest request = CreateRequest.newBuilder().setJson(msg).build();
        Integer channelId = protobuf.connect();
        protobuf.callAsync(channelId, request);
        Thread.sleep(500);
        ListResponse response = (ListResponse) protobuf.callSync(channelId, ListRequest.newBuilder().build());
        for (JsonMessage json : response.getJsonList()) {
            if(json.getId().equals(id)){
                return;
            }
        }
        fail("Missing json message");
    }

    /**
     * Test that jpa can be used from a eventbus endpoint.
     */
    @Test
    public void test_eventbus_and_jpa() throws Exception {
        String id = UUID.randomUUID().toString();
        final JsonEntity entity = new JsonEntity(id, Protocol.PROTOBUF, "msg");
        bus.send(ServerEndpoint.CREATE_EVENTBUS_ADDRESS, JsonUtil.toJson(entity));
        Thread.sleep(1000);
        class TestHandler implements Handler<Message<String>> {
            private JsonEntity result;

            @Override
            public void handle(Message<String> event) {
                this.result = JsonUtil.fromJson(event.body);
            }

            public JsonEntity getResult() {
                return result;
            }
        }
        TestHandler handler = new TestHandler();
        bus.send(ServerEndpoint.GET_EVENTBUS_ADDRESS, id, handler);
        Thread.sleep(1000);
        assertThat(entity, is(handler.getResult()));
    }

    /**
     * Test that jpa can be used from a job.
     */
    @Test
    public void test_job_and_jpa(){

    }




}
