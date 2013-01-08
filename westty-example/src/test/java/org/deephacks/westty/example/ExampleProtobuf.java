/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.deephacks.westty.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.deephacks.westty.Westty;
import org.deephacks.westty.config.JpaConfig;
import org.deephacks.westty.config.ServerConfig;
import org.deephacks.westty.example.CreateMessages.CreateRequest;
import org.deephacks.westty.example.CreateMessages.CreateResponse;
import org.deephacks.westty.example.DeleteMessages.DeleteRequest;
import org.deephacks.westty.example.DeleteMessages.DeleteResponse;
import org.deephacks.westty.protobuf.ProtobufException;
import org.deephacks.westty.protobuf.ProtobufSerializer;
import org.deephacks.westty.protobuf.WesttyProtobufClient;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class ExampleProtobuf {
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public static void main(String[] args) throws Exception {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);

        new JpaConfig().dropInstall();
        Westty westty = new Westty();
        westty.start();

        ProtobufSerializer serializer = new ProtobufSerializer();
        serializer.registerResource("META-INF/create.desc");
        serializer.registerResource("META-INF/delete.desc");
        serializer.registerResource("META-INF/failure.desc");
        WesttyProtobufClient client = new WesttyProtobufClient(Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool(), serializer);
        int clients = 2;
        CyclicBarrier barrier = new CyclicBarrier(clients + 1);
        for (int i = 0; i < clients; i++) {
            executor.execute(new Test(Integer.toString(i), client, barrier));
        }
        barrier.await();
        System.out.println("done");
        client.shutdown();
        westty.stop();

    }

    public static final class Test implements Runnable {
        private WesttyProtobufClient client;
        private String name;
        private CyclicBarrier barrier;

        public Test(String name, WesttyProtobufClient client, CyclicBarrier barrier) {
            this.client = client;
            this.name = name;
            this.barrier = barrier;
        }

        @Override
        public void run() {
            try {
                int port = new ServerConfig().getProtobufPort();
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
                    System.out.println(name + " done");
                } catch (Exception e) {

                }
            }

        }

    }
}
