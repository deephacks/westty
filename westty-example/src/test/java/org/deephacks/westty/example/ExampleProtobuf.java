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

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.deephacks.westty.Westty;
import org.deephacks.westty.config.JpaConfig;
import org.deephacks.westty.example.CreateMessages.CreateRequest;
import org.deephacks.westty.example.CreateMessages.CreateResponse;
import org.deephacks.westty.example.DeleteMessages.DeleteRequest;
import org.deephacks.westty.example.DeleteMessages.DeleteResponse;
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
        serializer.register(new File("src/main/resources/META-INF/create.desc"));
        serializer.register(new File("src/main/resources/META-INF/delete.desc"));
        int clients = 50;
        CyclicBarrier barrier = new CyclicBarrier(clients);
        for (int i = 0; i < clients; i++) {
            executor.execute(new Test(i + "", serializer, barrier));
        }
        barrier.await();
        System.out.println("done");
        westty.stop();
    }

    public static final class Test implements Runnable {
        private ProtobufSerializer serializer;
        private String name;
        private CyclicBarrier barrier;

        public Test(String name, ProtobufSerializer serializer, CyclicBarrier barrier) {
            this.serializer = serializer;
            this.name = name;
            this.barrier = barrier;
        }

        @Override
        public void run() {
            try {
                WesttyProtobufClient client = new WesttyProtobufClient(new InetSocketAddress(7777),
                        serializer);
                client.connect();
                for (int i = 0; i < 1000; i++) {
                    CreateRequest create = CreateRequest.newBuilder().setName(name)
                            .setPassword("pw").build();
                    DeleteRequest delete = DeleteRequest.newBuilder().setName(name).build();
                    CreateResponse cres = (CreateResponse) client.callSync(create);
                    if (!cres.getMsg().contains(name)) {
                        throw new RuntimeException();
                    }
                    DeleteResponse dres = (DeleteResponse) client.callSync(delete);
                    if (!dres.getMsg().contains(name)) {
                        throw new RuntimeException();
                    }

                }
                client.disconnect();
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
