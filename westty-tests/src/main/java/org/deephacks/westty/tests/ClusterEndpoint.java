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
package org.deephacks.westty.tests;

import org.deephacks.westty.cluster.Cluster;
import org.deephacks.westty.protobuf.Protobuf;
import org.deephacks.westty.protobuf.ProtobufMethod;
import org.deephacks.westty.server.Server;
import org.deephacks.westty.server.ServerName;
import org.deephacks.westty.sockjs.SockJsEndpoint;
import org.deephacks.westty.sockjs.SockJsMessage;
import org.deephacks.westty.tests.ClusterMessages.AsyncPublishRequest;
import org.deephacks.westty.tests.ClusterMessages.AsyncSendRequest;
import org.deephacks.westty.tests.ClusterMessages.GetPublishRequest;
import org.deephacks.westty.tests.ClusterMessages.GetPublishResponse;
import org.deephacks.westty.tests.ClusterMessages.GetSendRequest;
import org.deephacks.westty.tests.ClusterMessages.GetSendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Endpoint that test clustering. How ServerName are assigned to servers,
 * the view of the cluster for each server and that event bus communication
 * works correctly in the cluster.
 */
@Singleton
@Consumes({ APPLICATION_JSON })
@Produces({ APPLICATION_JSON })
@Path(ClusterEndpoint.JAXRS_PATH)
@Protobuf("cluster")
@SockJsEndpoint
public class ClusterEndpoint {
    public static final String JAXRS_PATH = "/jaxrs/cluster";
    public static final String SERVER_EVENTBUS_ADDRESS = "ClusterEndpoint";
    private Logger log = LoggerFactory.getLogger(ClusterEndpoint.class);

    @Inject
    private ServerName name;

    @Inject
    private Cluster cluster;

    @Inject
    private EventBus bus;

    private LinkedList<String> messages = new LinkedList<>();

    @GET
    @Path("/getServerName")
    public String getServerName() {
        return name.getName();
    }

    @GET
    @Path("/getClusterMemberPorts")
    public Set<Integer> getClusterMembers() {
        Set<Integer> ports = new HashSet<>();
        for (Server server : cluster.getMembers()){
            ports.add(server.getPort());
        }
        return ports;
    }


    @SockJsMessage(ClusterEndpoint.SERVER_EVENTBUS_ADDRESS)
    public void event(Message<String> msg){
        messages.addFirst(msg.body);
    }

    @ProtobufMethod
    public void send(AsyncSendRequest request) {
        bus.send(SERVER_EVENTBUS_ADDRESS, request.getMsg());
    }

    @ProtobufMethod
    public GetSendResponse getSend(GetSendRequest request) {
        List<String> msgs = Arrays.asList(messages.toArray(new String[0]));
        messages.clear();
        return GetSendResponse.newBuilder().addAllServerMsg(msgs).build();
    }

    @ProtobufMethod
    public void publish(AsyncPublishRequest request){
        bus.publish(SERVER_EVENTBUS_ADDRESS, request.getMsg());
    }

    @ProtobufMethod
    public GetPublishResponse getPublish(GetPublishRequest request){
        List<String> msgs = Arrays.asList(messages.toArray(new String[0]));
        messages.clear();
        return GetPublishResponse.newBuilder().addAllServerMsg(msgs).build();
    }

}
