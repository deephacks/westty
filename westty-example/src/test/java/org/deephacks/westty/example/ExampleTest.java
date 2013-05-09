package org.deephacks.westty.example;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.deephacks.westty.config.WebConfig;
import org.deephacks.westty.example.JaxrsClient.FormParam;
import org.deephacks.westty.example.protobuf.CreateMessages.CreateRequest;
import org.deephacks.westty.example.protobuf.CreateMessages.CreateResponse;
import org.deephacks.westty.example.protobuf.DeleteMessages.DeleteRequest;
import org.deephacks.westty.example.protobuf.DeleteMessages.DeleteResponse;
import org.deephacks.westty.jaxrs.JaxrsConfigClient;
import org.deephacks.westty.properties.WesttyProperties;
import org.deephacks.westty.protobuf.ProtobufConfig;
import org.deephacks.westty.protobuf.ProtobufException;
import org.deephacks.westty.protobuf.ProtobufSerializer;
import org.deephacks.westty.protobuf.WesttyProtobufClient;
import org.deephacks.westty.test.SQLExec;
import org.deephacks.westty.test.WesttyTestBootstrap;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

//@RunWith(WesttyJUnit4Runner.class)
public class ExampleTest {
    private static final String host = "localhost";
    private static final int port = 8080;
    private static final JaxrsClient client = new JaxrsClient(host, port);
    private static final JaxrsConfigClient configClient = new JaxrsConfigClient(host, port);

    @WesttyTestBootstrap
    public static void bootstrap() {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        WesttyProperties.setBinDir(new File("/tmp"));
    }

    private static SQLExec bootstrap = new SQLExec("westty", "westty",
            "jdbc:derby:memory:westty;create=true");
    static {
        try {
            bootstrap.executeResource("META-INF/install_config_derby.ddl", false);
            bootstrap.executeResource("META-INF/install_example_derby.ddl", false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void test_protobuf() throws Exception {
        ProtobufSerializer serializer = new ProtobufSerializer();
        serializer.registerResource("META-INF/create.desc");
        serializer.registerResource("META-INF/delete.desc");
        serializer.registerResource("META-INF/failure.desc");
        ExecutorService executor = Executors.newCachedThreadPool();
        WesttyProtobufClient client = new WesttyProtobufClient(Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool(), serializer);
        int channelId = client.connect(new InetSocketAddress(host, port));
        try {
            int clients = 1;
            CyclicBarrier barrier = new CyclicBarrier(clients + 1);
            for (int i = 0; i < clients; i++) {
                executor.execute(new ProtobufClient(Integer.toString(i), client, barrier));
            }
            barrier.await();
            System.out.println("done");
            client.shutdown();
        } finally {
            client.disconnect(channelId);
        }
    }

    public void test_jaxrs_auth() throws Exception {
        FormParam u = new FormParam("username", "u");
        FormParam p = new FormParam("password", "p");
        client.postHttpForm("/jaxrs/auth-service/createUser", u, p);
        //        client.postHttpForm("/jaxrs/auth-service/sessionLogin", u, p);
        //        client.postHttpForm("/jaxrs/auth-service/cookieLogin", u, p);
    }

    public void test_set_config() {
        WebConfig config = new WebConfig();
        config.setUri("test");
        configClient.set(config);
    }

    public static final class ProtobufClient implements Runnable {
        private WesttyProtobufClient client;
        private String name;
        private CyclicBarrier barrier;

        public ProtobufClient(String name, WesttyProtobufClient client, CyclicBarrier barrier) {
            this.client = client;
            this.name = name;
            this.barrier = barrier;
        }

        @Override
        public void run() {
            try {
                int port = new ProtobufConfig().getPort();
                int channel = client.connect(new InetSocketAddress(port));
                for (int i = 0; i < 1000; i++) {
                    CreateRequest create = CreateRequest.newBuilder().setName(name)
                            .setPassword("pw").build();
                    DeleteRequest delete = DeleteRequest.newBuilder().setName(name).build();
                    try {
                        CreateResponse cres = (CreateResponse) client.callSync(channel, create);
                        if (!cres.getMsg().contains(name)) {
                            throw new RuntimeException();
                        }
                        DeleteResponse dres = (DeleteResponse) client.callSync(channel, delete);
                        if (!dres.getMsg().contains(name)) {
                            throw new RuntimeException();
                        }
                    } catch (ProtobufException e) {
                        e.printStackTrace();
                        return;
                    }

                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    barrier.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * Compute the root directory of this maven project. This will result in the
     * same directory no matter if executed from Eclipse, this maven project
     * root or any parent maven pom directory.
     * 
     * @param anyTestClass
     *            Any test class *local* to the maven project, i.e that only
     *            exist in this maven project.
     * 
     * @param child
     *            The file that should be
     * @return The root directory of this maven project.
     */
    public static File computeMavenProjectRoot(Class<?> anyTestClass) {
        final String clsUri = anyTestClass.getName().replace('.', '/') + ".class";
        final URL url = anyTestClass.getClassLoader().getResource(clsUri);
        final String clsPath = url.getPath();
        // located in ./target/test-classes or ./eclipse-out/target
        final File target_test_classes = new File(clsPath.substring(0,
                clsPath.length() - clsUri.length()));
        // get parent's parent
        return target_test_classes.getParentFile().getParentFile();
    }

    /**
     * Normalizes the root for reading a file to the maven project root
     * directory.
     * 
     * @param anyTestClass
     *            Any test class *local* to the maven project, i.e that only
     *            exist in this maven project.
     * 
     * @param child
     *            A child path.
     * 
     * @return A file relative to the maven root.
     */
    public static File computeMavenProjectRoot(Class<?> anyTestClass, String child) {
        return new File(computeMavenProjectRoot(anyTestClass), child);
    }

}
