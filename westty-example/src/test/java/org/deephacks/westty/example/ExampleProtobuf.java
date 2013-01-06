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
import java.net.InetSocketAddress;

import org.deephacks.westty.example.CreateMessages.CreateRequest;
import org.deephacks.westty.protobuf.ProtobufRpcClient;
import org.deephacks.westty.protobuf.ProtobufSerializer;

public class ExampleProtobuf {
    public static void main(String[] args) throws Exception {
        ProtobufSerializer serializer = new ProtobufSerializer();
        serializer.register(new File("src/main/resources/META-INF/create.desc"));
        ProtobufRpcClient client = new ProtobufRpcClient(new InetSocketAddress(7777), serializer);
        client.connect();
        CreateRequest req = CreateRequest.newBuilder().setName("name").setPassword("pw").build();
        client.write(req);
        Thread.sleep(10000);

    }
}
