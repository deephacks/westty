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
package org.deephacks.westty.internal.jaxrs;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.deephacks.tools4j.config.internal.core.Reflections.forName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.deephacks.tools4j.config.admin.AdminContext;
import org.deephacks.tools4j.config.internal.core.runtime.ObjectToBeanConverter;
import org.deephacks.tools4j.config.model.Bean;
import org.deephacks.tools4j.config.model.Bean.BeanId;
import org.deephacks.tools4j.config.model.Lookup;
import org.deephacks.tools4j.config.model.Schema;
import org.deephacks.tools4j.config.spi.Conversion;
import org.deephacks.westty.jaxrs.JaxrsConfigBean;
import org.deephacks.westty.jaxrs.JaxrsConfigClient;
import org.deephacks.westty.persistence.Transactional;

/**
 * JAX-RS endpoint for provisioning configuration in JSON format.
 */
@Path(JaxrsConfigClient.PATH)
@Consumes({ APPLICATION_JSON })
@Produces({ APPLICATION_JSON })
public class WesttyJaxrsConfigEndpoint {

    private static final AdminContext ctx = Lookup.get().lookup(AdminContext.class);
    private static final Conversion conv = Conversion.get();
    static {
        conv.register(new ObjectToBeanConverter());
    }

    @GET
    @Path("/")
    public Map<String, Schema> getSchema() {
        return ctx.getSchemas();
    }

    /**
     * See AdminContext.get for more information.
     * 
     * @param schema
     * @param id
     * @return
     */
    @GET
    @Path("get/{schema}/{id}")
    @Transactional
    public Object get(@PathParam("schema") final String schema, @PathParam("id") final String id) {
        BeanId beanId = BeanId.create(id, schema);
        Bean bean = ctx.get(beanId);
        return conv.convert(bean, forName(schema));
    }

    /**
     * See AdminContext.list for more information.
     * 
     * @param schema assumes to be the fully classified class name.
     * @return list of configurables objects
     */
    @GET
    @Path("list/{schema}")
    @Produces({ APPLICATION_JSON })
    @Transactional
    public List<Object> list(@PathParam("schema") final String schema) {
        List<Bean> beans = ctx.list(schema);
        Collection<?> o = conv.convert(beans, forName(schema));
        return new ArrayList<Object>(o);
    }

    /**
     * See AdminContext.create for more information.
     * 
     * @param jaxrsBean wrapper class for the configurable object.
     */
    @POST
    @Consumes({ APPLICATION_JSON })
    @Path("create")
    public void create(final JaxrsConfigBean jaxrsBean) {
        Bean bean = conv.convert(jaxrsBean.getBean(), Bean.class);
        ctx.create(bean);
    }

    /**
     * See AdminContext.set for more information.
     * 
     * @param jaxrsBean wrapper class for the configurable object.
     */
    @POST
    @Consumes({ APPLICATION_JSON })
    @Path("set")
    @Transactional
    public void set(final JaxrsConfigBean jaxrsBean) {
        Bean bean = conv.convert(jaxrsBean.getBean(), Bean.class);
        ctx.set(bean);
    }

    /**
     * See AdminContext.merge for more information.
     * 
     * @param jaxrsBean wrapper class for the configurable object.
     */
    @POST
    @Consumes({ APPLICATION_JSON })
    @Path("merge")
    @Transactional
    public void merge(final JaxrsConfigBean jaxrsBean) {
        Bean bean = conv.convert(jaxrsBean.getBean(), Bean.class);
        ctx.merge(bean);
    }

    /**
     * See AdminContext.delete for more information.
     * 
     * @param schema schema name of instance to be removed
     * @param id id of the instance to be removed
     */
    @DELETE
    @Path("delete/{schema}/{id}")
    public void delete(@PathParam("schema") final String schema, @PathParam("id") final String id) {
        BeanId beanId = BeanId.create(id, schema);
        ctx.delete(beanId);
    }

}
