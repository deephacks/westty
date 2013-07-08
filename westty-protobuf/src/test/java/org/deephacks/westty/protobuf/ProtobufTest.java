package org.deephacks.westty.protobuf;

import com.google.common.base.Optional;
import org.deephacks.westty.protobuf.CreateMessages.AsyncCreateRequest;
import org.deephacks.westty.protobuf.CreateMessages.CreateExceptionRequest;
import org.deephacks.westty.protobuf.CreateMessages.CreateRequest;
import org.deephacks.westty.protobuf.CreateMessages.CreateResponse;
import org.deephacks.westty.protobuf.CreateMessages.GetRequest;
import org.deephacks.westty.protobuf.CreateMessages.NullRequest;
import org.deephacks.westty.protobuf.DeleteMessages.DeleteRequest;
import org.deephacks.westty.protobuf.DeleteMessages.DeleteResponse;
import org.deephacks.westty.test.WesttyJUnit4Runner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

@RunWith(WesttyJUnit4Runner.class)
public class ProtobufTest {

    @Inject
    /** make sure that we can use injection for the client */
    private ProtobufClient client;

    /** the id of the channel used by the tests */
    private int channelId;

    /**
     * Client connect is tested here.
     */
    @Before
    public void before() throws Exception {
        channelId = client.connect();
    }

    /**
     * Client disconnect is tested here.
     */
    @After
    public void after() throws Exception {
        client.disconnect(channelId);
    }

    /**
     * Test a sync endpoint invocation.
     */
    @Test
    public void test_sync_create() throws Exception {
        String msg = "msg";
        CreateRequest req = CreateRequest.newBuilder()
                .setMsg(msg)
                .build();
        CreateResponse res = (CreateResponse) client.callSync(channelId, req);
        assertThat(res.getMsg(), is(msg));
    }

    /**
     * Test that methods can be called async, verify that the endpoint
     * really got the message by fetching it shortly after the request.
     */
    @Test
    public void test_async_create() throws Exception {
        String msg = "msg";
        AsyncCreateRequest req = AsyncCreateRequest.newBuilder()
                .setMsg(msg)
                .build();
        client.callAsync(channelId, req);
        Thread.sleep(500);
        GetRequest get = GetRequest.newBuilder().build();
        req = (AsyncCreateRequest) client.callSync(channelId, get);
        assertThat(req.getMsg(), is(msg));
    }

    /**
     * Test that sync endpoints can be invoked async, the
     * response will be ignored.
     */
    @Test
    public void test_async_sync_create() throws Exception {
        String msg = "msg";
        CreateRequest req = CreateRequest.newBuilder()
                .setMsg(msg)
                .build();
        client.callAsync(channelId, req);
        Thread.sleep(500);
        GetRequest get = GetRequest.newBuilder().build();
        req = (CreateRequest) client.callSync(channelId, get);
        assertThat(req.getMsg(), is(msg));
    }

    /**
     * Test that a endpoint that return a null response will
     * deliver a void message that will be transalted to null
     * for the client.
     */
    @Test
    public void test_null_response() throws Exception {
        NullRequest req = NullRequest.newBuilder()
                .build();
        Object res = client.callSync(channelId, req);
        assertNull(res);
    }

    /**
     * Test that proto messages can be declared in multiple .proto and .desc files.
     *
     * (The delete message is declared in a separated .proto file)
     */
    @Test
    public void test_delete() throws Exception {
        String msg = "msg";
        DeleteRequest req = DeleteRequest.newBuilder()
                .setMsg(msg)
                .build();
        DeleteResponse res = (DeleteResponse) client.callSync(channelId, req);
        assertThat(res.getMsg(), is(msg));
    }

    /**
     * Test that exceptions a delivered as FailureMessages that will be translated
     * to a FailureMessageException to the client and that the correct error code
     * is delivered with it.
     */
    @Test
    public void test_exception() throws Exception {
        String name = "name";
        CreateExceptionRequest req = CreateExceptionRequest.newBuilder()
                .setMsg(name)
                .build();
        try {
            client.callSync(channelId, req);
            fail("This invocation should cause an exception");
        } catch (FailureMessageException e) {
            assertThat(e.getCode(), is(Integer.MIN_VALUE));
        }
    }

    /**
     * Test that multiple clients can connect through same ProtobufClient
     * and that concurrent communication with endpoints deliver callbacks
     * to the correct clients.
     */
    @Test
    public void test_multiple_clients() throws Exception {
        int numClients = 20;
        Executor executor = Executors.newFixedThreadPool(numClients);
        List<Client> clients = new ArrayList<>(numClients);
        CountDownLatch latch = new CountDownLatch(numClients);
        for (int i = 0; i < numClients; i++) {
            Client c = new Client(client);
            clients.add(c);
            executor.execute(c.connect(latch));
        }
        latch.await();

        int numMessages = 1000;
        latch = new CountDownLatch(numClients * numMessages);
        for (int i = 0; i < numMessages; i++) {
           for (Client c : clients){
               executor.execute(c.sendMessage(latch));
           }
        }
        latch.await();
        latch = new CountDownLatch(numClients);
        StringBuilder failureMessages = new StringBuilder();

        for (Client c : clients) {
            if(c.getIncorrectMessage().isPresent()){
                failureMessages.append(c.getIncorrectMessage().get());
            }
            executor.execute(c.disconnect(latch));
        }

        if(failureMessages.length() > 0){
            fail(failureMessages.toString());
        }
    }

    public static class Client {
        private ProtobufClient client;
        private int channelId;
        private String id = UUID.randomUUID().toString();
        private AtomicLong counter = new AtomicLong();
        private Optional<String> incorrectMessage = Optional.absent();

        public Client(ProtobufClient client){
            this.client = client;
        }
        public Optional<String> getIncorrectMessage(){
            return incorrectMessage;
        }
        public Runnable connect(final CountDownLatch latch) throws IOException {
            return new Runnable() {
                @Override
                public void run() {
                    try {
                        channelId = client.connect();
                        latch.countDown();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };

        }

        public Runnable disconnect(final CountDownLatch latch) {
            return new Runnable() {
                @Override
                public void run() {
                    client.disconnect(channelId);
                }
            };
        }

        /**
         * Send a message and make assert that we got the correct response back
         * from the callback.
         */
        public Runnable sendMessage(final CountDownLatch latch) throws IOException {
            return new Runnable() {
                @Override
                public void run() {
                    String msg = id + counter.incrementAndGet();
                    CreateRequest req = CreateRequest.newBuilder()
                            .setMsg(msg)
                            .build();
                    try {
                        CreateResponse res = (CreateResponse) client.callSync(channelId, req);
                        latch.countDown();
                        if(!res.getMsg().equals(msg)){
                            String message = " Expected " + msg + " but got " + res.getMsg() + ". ";
                            incorrectMessage = Optional.of(message);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        }
    }
}
