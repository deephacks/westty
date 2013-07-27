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

import org.deephacks.confit.ConfigContext;
import org.deephacks.westty.job.Job;
import org.deephacks.westty.job.JobData;
import org.deephacks.westty.job.Schedule;
import org.deephacks.westty.jpa.Transactional;
import org.deephacks.westty.protobuf.Protobuf;
import org.deephacks.westty.protobuf.ProtobufMethod;
import org.deephacks.westty.sockjs.SockJsEndpoint;
import org.deephacks.westty.sockjs.SockJsMessage;
import org.deephacks.westty.tests.JsonEntity.Protocol;
import org.deephacks.westty.tests.ServerMessages.CreateRequest;
import org.deephacks.westty.tests.ServerMessages.JsonMessage;
import org.deephacks.westty.tests.ServerMessages.ListRequest;
import org.deephacks.westty.tests.ServerMessages.ListResponse;
import org.deephacks.westty.tests.ServerMessages.ListResponse.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Endpoint that use all the features available in westty in
 * order to test how they integrate. This is only a test. This
 * kind of endpoints is not advised.
 */
@Singleton
@Path(ServerEndpoint.JAXRS_PATH)
@Protobuf("server")
@Schedule("*/2 * * * * ?")
@SockJsEndpoint
public class ServerEndpoint implements Job {
    public static final String JAXRS_PATH = "/server";
    public static final String CREATE_EVENTBUS_ADDRESS = "CreateServerEndpoint";
    public static final String GET_EVENTBUS_ADDRESS = "GetServerEndpoint";

    private Logger log = LoggerFactory.getLogger(ServerEndpoint.class);

    @Inject
    private EventBus bus;

    @Inject
    private EntityManager em;

    @Inject
    private Parent parent;

    @Inject
    private ConfigContext config;

    @Transactional
    public void createJpa(JsonEntity entity) {
        Map<String, Child> children = parent.getChildren();
        for (String id : children.keySet()) {
            em.persist(entity);
        }
    }

    @GET
    @Path("list")
    @Produces({ APPLICATION_JSON })
    @Transactional
    public List<JsonEntity> list() {
        return (List<JsonEntity>) em.createQuery("select e from JsonEntity e", JsonEntity.class).getResultList();
    }

    @POST
    @Path("create")
    @Consumes({ APPLICATION_JSON })
    @Transactional
    public void create(JsonEntity entity) {
        em.persist(entity);
    }

    @POST
    @Path("nested")
    @Consumes({ APPLICATION_JSON })
    @Transactional
    public void nestedRollback(JsonEntity entity) {
        nestedCreate(entity);
        em.getTransaction().rollback();
    }


    @Transactional
    public void nestedCreate(JsonEntity entity){
        createJpa(entity);
    }


    @SockJsMessage(CREATE_EVENTBUS_ADDRESS)
    @Transactional
    public void create(Message<String> msg){
        JsonEntity entity = JsonUtil.fromJson(msg.body);
        em.persist(entity);
    }

    @SockJsMessage(GET_EVENTBUS_ADDRESS)
    @Transactional
    public void get(Message<String> msg){
        Query q = em.createQuery("select e from JsonEntity e where e.id = :id", JsonEntity.class);
        q.setParameter("id", msg.body);
        List<JsonEntity> list = (List<JsonEntity>) q.getResultList();
        JsonEntity first = list.get(0);
        msg.reply(JsonUtil.toJson(first));
    }

    @ProtobufMethod
    @Transactional
    public void create(CreateRequest request) {
        JsonMessage msg = request.getJson();
        em.persist(new JsonEntity(msg.getId(), Protocol.PROTOBUF, msg.getJson()));
    }

    @ProtobufMethod
    @Transactional
    public ListResponse list(ListRequest request) {
        List<JsonEntity> list = (List<JsonEntity>) em.createQuery("select e from JsonEntity e", JsonEntity.class).getResultList();
        Builder builder = ListResponse.newBuilder();
        for (JsonEntity entity : list) {
            JsonMessage msg = JsonMessage.newBuilder().setId(entity.getId()).setJson(entity.getJson()).build();
            builder.addJson(msg);
        }
        return builder.build();
    }

    @Override
    @Transactional
    public void execute(JobData map) {
    }

}
