package org.deephacks.westty.tests;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.deephacks.tools4j.config.spi.BeanManager;
import org.deephacks.westty.Westty;
import org.deephacks.westty.config.ClusterConfig;
import org.deephacks.westty.config.ProtobufConfig;
import org.deephacks.westty.config.ServerConfig;
import org.deephacks.westty.config.SockJsConfig;
import org.deephacks.westty.jaxrs.JaxrsConfigClient;
import org.deephacks.westty.protobuf.ProtobufClient;
import org.deephacks.westty.protobuf.ProtobufSerializer;
import org.deephacks.westty.spi.IoExecutors;
import org.deephacks.westty.tests.ClusterMessages.AsyncPublishRequest;
import org.deephacks.westty.tests.ClusterMessages.AsyncSendRequest;
import org.deephacks.westty.tests.ClusterMessages.GetPublishRequest;
import org.deephacks.westty.tests.ClusterMessages.GetPublishResponse;
import org.deephacks.westty.tests.ClusterMessages.GetSendRequest;
import org.deephacks.westty.tests.ClusterMessages.GetSendResponse;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Tests that a cluster of instances can be setup programmatically on the
 * same machine without port conflicts. Do note that all servers will be
 * running in the same class loader, so the tests will try to verify that
 * servers are isolated from one another.
 *
 * TODO: These tests work, but shutdown of westty does not, so there will be port conflicts
 */
public class ClusterIntegrationTest {
    private static final ProtobufSerializer serializer = new ProtobufSerializer();
    static {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        serializer.registerResource("META-INF/cluster.desc");
    }
    private static ProtobufConfig protobufConfig = new ProtobufConfig();
    private static final ProtobufClient protobufClient = new ProtobufClient(new IoExecutors(), serializer, new ProtobufConfig());
    private static final JaxrsConfigClient jaxrsConfigClient = new JaxrsConfigClient();

    private static final Westty w1 = new Westty("w1");
    private static final Westty w2 = new Westty("w2");
    private static final Westty w3 = new Westty("w3");

    // @BeforeClass
    public static void beforeClass() throws Throwable {
        // make sure that we can force the xml bean manager
        // even though the jpa bean manager normally would
        // take precedence..
        System.setProperty(BeanManager.class.getName(),
                "org.deephacks.tools4j.config.internal.core.xml.XmlBeanManager");
        w1.startup();
        createServers("w1", "w2", "w3");
        w2.startup();
        w3.startup();
    }

    // @AfterClass
    public static void afterClass() throws Throwable {
        w1.shutdown();
        w2.shutdown();
        w3.shutdown();
    }
    /**
     * Test jaxrs and that each server have correct ServerName.
     */
    // @Test
    public void test_servers_have_correct_names() {
        String path = ClusterEndpoint.JAXRS_PATH + "/getServerName";
        String serverName = httpGet(path, 0);
        assertThat(serverName, is("w1"));
        serverName = httpGet(path, 1);
        assertThat(serverName, is("w2"));
        serverName = httpGet(path, 2);
        assertThat(serverName, is("w3"));
    }

    /**
     * Test jaxrs and that each server have a correct view of members in
     * the cluster.
     */
    // @Test
    public void test_servers_have_correct_cluster_memberhip_view(){
        String path = ClusterEndpoint.JAXRS_PATH + "/getClusterMemberPorts";
        int base = ServerConfig.DEFAULT_CLUSTER_PORT;
        // w1
        List<Integer> ports = JsonUtil.getPorts(httpGet(path, 0));
        assertTrue(ports.containsAll(Arrays.asList(base,base+1,base+2)));
        // w2
        ports = JsonUtil.getPorts(httpGet(path, 1));
        assertTrue(ports.containsAll(Arrays.asList(base,base+1,base+2)));
        // w3
        ports = JsonUtil.getPorts(httpGet(path, 2));
        assertTrue(ports.containsAll(Arrays.asList(base,base+1,base+2)));
    }
    /**
     * Test that each server can receive a protobuf message and send it forward
     * onto the eventbus message to be received by any server (but only one)
     * in the cluster.
     */
    // @Test
    public void test_servers_eventbus_send_to_cluster_members() throws Exception {
        String msg = "w1";
        // w1
        asyncSendProtobuf(msg, 0);
        List<String> all = new ArrayList<>();
        all.addAll(getSendProtobuf(0));
        all.addAll(getSendProtobuf(1));
        all.addAll(getSendProtobuf(2));
        assertThat(all.size(), is(1));
        assertThat(all.get(0), is(msg));
        // w2
        msg = "w2";
        asyncSendProtobuf(msg, 1);
        all = new ArrayList<>();
        all.addAll(getSendProtobuf(0));
        all.addAll(getSendProtobuf(1));
        all.addAll(getSendProtobuf(2));
        assertThat(all.size(), is(1));
        assertThat(all.get(0), is(msg));
        // w3
        msg = "w3";
        asyncSendProtobuf(msg, 2);
        all = new ArrayList<>();
        all.addAll(getSendProtobuf(0));
        all.addAll(getSendProtobuf(1));
        all.addAll(getSendProtobuf(2));
        assertThat(all.size(), is(1));
        assertThat(all.get(0), is(msg));
    }

    /**
     * Test that each server can receive a protobuf message and forward
     * it onto the eventbus message to be published to all members in the cluster.
     */
    // @Test
    public void test_servers_eventbus_publish_to_cluster_members() throws Exception {
        String msg = "w1";
        // w1
        asyncPublishProtobuf(msg, 0);
        List<String> msgs = getPublishProtobuf(0);
        assertThat(msgs.size(), is(1));
        assertThat(msgs.get(0), is(msg));
        msgs = getPublishProtobuf(1);
        assertThat(msgs.size(), is(1));
        assertThat(msgs.get(0), is(msg));
        msgs = getPublishProtobuf(2);
        assertThat(msgs.size(), is(1));
        assertThat(msgs.get(0), is(msg));
        // w2
        msg = "w2";
        asyncPublishProtobuf(msg, 1);
        msgs = getPublishProtobuf(0);
        assertThat(msgs.size(), is(1));
        assertThat(msgs.get(0), is(msg));
        msgs = getPublishProtobuf(1);
        assertThat(msgs.size(), is(1));
        assertThat(msgs.get(0), is(msg));
        msgs = getPublishProtobuf(2);
        assertThat(msgs.size(), is(1));
        assertThat(msgs.get(0), is(msg));
        // w3
        msg = "w3";
        asyncPublishProtobuf(msg, 2);
        msgs = getPublishProtobuf(0);
        assertThat(msgs.size(), is(1));
        assertThat(msgs.get(0), is(msg));
        msgs = getPublishProtobuf(1);
        assertThat(msgs.size(), is(1));
        assertThat(msgs.get(0), is(msg));
        msgs = getPublishProtobuf(2);
        assertThat(msgs.size(), is(1));
        assertThat(msgs.get(0), is(msg));

    }


    private String httpGet(String path, int num) {
        JaxrsClient client = new JaxrsClient(ServerConfig.DEFAULT_IP_ADDRESS, ServerConfig.DEFAULT_HTTP_PORT + num);
        return client.gethttp(path);
    }

    private void asyncSendProtobuf(String msg, Integer num) throws Exception {
        Integer channelId = protobufClient.connect(new InetSocketAddress(protobufConfig.getPort() + num));
        AsyncSendRequest req = AsyncSendRequest.newBuilder().setMsg(msg).build();
        protobufClient.callAsync(channelId, req);
        Thread.sleep(500);
        protobufClient.disconnect(channelId);
    }

    private List<String> getSendProtobuf(Integer num) throws IOException {
        Integer channelId = protobufClient.connect(new InetSocketAddress(protobufConfig.getPort() + num));
        GetSendRequest req = GetSendRequest.newBuilder().build();
        GetSendResponse res = (GetSendResponse) protobufClient.callSync(channelId, req);
        protobufClient.disconnect(channelId);
        return res.getServerMsgList();
    }

    private void asyncPublishProtobuf(String msg, Integer num) throws Exception {
        Integer channelId = protobufClient.connect(new InetSocketAddress(protobufConfig.getPort() + num));
        AsyncPublishRequest req = AsyncPublishRequest.newBuilder().setMsg(msg).build();
        protobufClient.callAsync(channelId, req);
        Thread.sleep(500);
        protobufClient.disconnect(channelId);
    }

    private List<String> getPublishProtobuf(Integer num) throws IOException {
        Integer channelId = protobufClient.connect(new InetSocketAddress(protobufConfig.getPort() + num));
        GetPublishRequest req = GetPublishRequest.newBuilder().build();
        GetPublishResponse res = (GetPublishResponse) protobufClient.callSync(channelId, req);
        protobufClient.disconnect(channelId);
        return res.getServerMsgList();
    }
    public static void createServers(String... serverNames) throws Throwable {
        ClusterConfig cluster = new ClusterConfig();
        for (int i = 0; i < serverNames.length; i++){
            ServerConfig server = new ServerConfig(serverNames[i]);
            server.setHttpPort(ServerConfig.DEFAULT_HTTP_PORT + i);
            server.setClusterPort(ServerConfig.DEFAULT_CLUSTER_PORT + i);
            jaxrsConfigClient.create(server);
            cluster.addServer(server);

            ProtobufConfig proto = new ProtobufConfig(serverNames[i]);
            proto.setPort(protobufConfig.getPort() + i);
            jaxrsConfigClient.create(proto);

            SockJsConfig sockjs = new SockJsConfig(serverNames[i]);
            sockjs.setHttpPort(SockJsConfig.DEFAULT_HTTP_PORT + i);
            sockjs.setEventBusPort(SockJsConfig.DEFAULT_EVENTBUS_PORT + i);
            jaxrsConfigClient.create(sockjs);
        }

        jaxrsConfigClient.create(cluster);
    }
}
