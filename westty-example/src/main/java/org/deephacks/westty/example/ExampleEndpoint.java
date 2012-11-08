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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.deephacks.westty.jpa.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Consumes({ APPLICATION_JSON })
@Produces({ "application/json" })
@Path("example")
@Singleton
public class ExampleEndpoint {
    private Logger log = LoggerFactory.getLogger(ExampleEndpoint.class);

    @Inject
    private EntityManager em;

    @GET
    @Path("/get/{id}")
    @Transactional
    public ExampleEntity get(@PathParam("id") String id) {
        ExampleEntity o = em.find(ExampleEntity.class, id);
        log.debug("Query {}={}", id, o);
        if (o == null) {
            return new ExampleEntity("", "");
        }
        return o;
    }

    @POST
    @Path("/create/{id}")
    @Transactional
    public void create(@PathParam("id") String id) {
        ExampleEntity entity = new ExampleEntity(id, "config prop, fixme");
        em.persist(entity);
        log.debug("persisted successful {}", entity);
    }

}
