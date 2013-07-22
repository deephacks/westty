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
package org.deephacks.westty.internal.jaxrs.config;

import com.google.common.base.Optional;
import org.deephacks.tools4j.config.admin.AdminContext;
import org.deephacks.tools4j.config.internal.core.runtime.BeanToObjectConverter;
import org.deephacks.tools4j.config.internal.core.runtime.ClassToSchemaConverter;
import org.deephacks.tools4j.config.internal.core.runtime.FieldToSchemaPropertyConverter;
import org.deephacks.tools4j.config.internal.core.runtime.ObjectToBeanConverter;
import org.deephacks.tools4j.config.model.Bean;
import org.deephacks.tools4j.config.model.Bean.BeanId;
import org.deephacks.tools4j.config.model.Events;
import org.deephacks.tools4j.config.model.Schema;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyRef;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyRefList;
import org.deephacks.tools4j.config.model.Schema.SchemaPropertyRefMap;
import org.deephacks.tools4j.config.spi.Conversion;
import org.deephacks.westty.jaxrs.JaxrsConfigBeans;
import org.deephacks.westty.jaxrs.JaxrsConfigBeans.JaxrsConfigBean;
import org.deephacks.westty.jaxrs.JaxrsConfigClient;
import org.deephacks.westty.jaxrs.JaxrsConfigObjects;
import org.deephacks.westty.jaxrs.JaxrsConfigObjects.JaxrsConfigObject;
import org.deephacks.westty.jaxrs.JaxrsSchema;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static org.deephacks.tools4j.config.internal.core.Reflections.forName;

/**
 * JAX-RS endpoint for provisioning configuration in JSON format.
 */
@Path(JaxrsConfigClient.PATH)
@Consumes({ APPLICATION_JSON })
@Produces({ APPLICATION_JSON })
public class JaxrsConfigEndpoint {

    private AdminContext admin;

    private static final Conversion conv = Conversion.get();
    private static final Map<String, Schema> schemas = new HashMap<>();
    static {
        conv.register(new ObjectToBeanConverter());
        conv.register(new ClassToSchemaConverter());
        conv.register(new FieldToSchemaPropertyConverter());
        conv.register(new BeanToObjectConverter());
    }

    @Inject
    public JaxrsConfigEndpoint(AdminContext ctx) {
        this.admin = ctx;
        for (Schema s : ctx.getSchemas().values()) {
            schemas.put(s.getType(), s);
        }
    }

    @GET
    @Path("/getschemas")
    @Produces({ APPLICATION_JSON })
    public Map<String, JaxrsSchema> getSchemas() {
        Map<String, JaxrsSchema> schemas = new HashMap<>();
        for (Schema schema : admin.getSchemas().values()) {
            schemas.put(schema.getName(), new JaxrsSchema(schema));
        }
        return schemas;
    }

    @GET
    @Path("get/{className}/{id}")
    public Object get(@PathParam("className") final String className,
            @PathParam("id") final String id) {
        Schema schema = schemas.get(className);
        BeanId beanId = BeanId.create(id, schema.getName());
        Optional<Bean> optional = admin.get(beanId);
        if (!optional.isPresent()) {
            throw Events.CFG304_BEAN_DOESNT_EXIST(beanId);
        }
        return conv.convert(optional.get(), forName(className));
    }

    @GET
    @Path("getSingleton/{className}")
    public Object get(@PathParam("className") final String className) {
        Schema schema = schemas.get(className);
        BeanId beanId = BeanId.createSingleton(schema.getName());
        Bean bean = admin.get(beanId).get();
        return conv.convert(bean, forName(className));
    }

    @GET
    @Path("get/{className}")
    @Produces(APPLICATION_JSON)
    public JaxrsConfigObjects get(@PathParam("className") final String className,
            @QueryParam("id") final List<String> ids) {
        Schema schema = schemas.get(className);
        List<Bean> result = admin.list(schema.getName(), ids);
        JaxrsConfigObjects beans = new JaxrsConfigObjects();
        if (result.isEmpty()) {
            return beans;
        }
        Collection<?> o = conv.convert(result, forName(className));
        beans.setBeans(o);
        return beans;
    }

    @GET
    @Path("list/{className}")
    @Produces({ APPLICATION_JSON })
    public JaxrsConfigObjects list(@PathParam("className") final String className) {
        Schema schema = schemas.get(className);
        List<Bean> beans = admin.list(schema.getName());
        JaxrsConfigObjects result = new JaxrsConfigObjects();
        if (beans.isEmpty()) {
            return result;
        }
        Collection<?> o = conv.convert(beans, forName(className));
        result.setBeans(o);
        return result;
    }

    @GET
    @Path("paginate/{className}")
    @Produces({ APPLICATION_JSON })
    public JaxrsConfigObjects paginate(@PathParam("className") final String className,
            @QueryParam("first") int first, @QueryParam("max") int max,
            @QueryParam("prop") String prop, @QueryParam("targetClassName") String targetClassName,
            @QueryParam("id") String id) {
        /**
         * Fixme
         */
        return null;
    }

    @POST
    @Consumes({ APPLICATION_JSON })
    @Path("create")
    public void create(final JaxrsConfigObject jaxrsBean) {
        Bean bean = conv.convert(jaxrsBean.getBean(), Bean.class);
        admin.create(bean);
    }

    @POST
    @Consumes({ APPLICATION_JSON })
    @Path("set")
    public void set(final JaxrsConfigObject jaxrsBean) {
        Bean bean = conv.convert(jaxrsBean.getBean(), Bean.class);
        admin.set(bean);
    }

    @POST
    @Consumes({ APPLICATION_JSON })
    @Path("merge")
    public void merge(final JaxrsConfigObject jaxrsBean) {
        Bean bean = conv.convert(jaxrsBean.getBean(), Bean.class);
        admin.merge(bean);
    }

    @DELETE
    @Consumes({ APPLICATION_XML })
    @Path("delete/{schema}/{id}")
    public void delete(@PathParam("schema") final String className, @PathParam("id") final String id) {
        Schema schema = schemas.get(className);
        BeanId beanId = BeanId.create(id, schema.getName());
        admin.delete(beanId);
    }

    @POST
    @Path("createbean")
    @Consumes({ APPLICATION_JSON })
    public void createbean(final JaxrsConfigBean bean) {
        admin.create(jaxrsToBean(bean));
    }

    @GET
    @Path("getbean/{schema}/{id}")
    public JaxrsConfigBean getbean(@PathParam("schema") final String schema,
            @PathParam("id") final String id) {
        BeanId beanId = BeanId.create(id, schema);
        Bean bean = admin.get(beanId).get();
        return new JaxrsConfigBean(bean);
    }

    @GET
    @Path("listbeans/{schema}")
    @Produces({ APPLICATION_JSON })
    public JaxrsConfigBeans listbeans(@PathParam("schema") final String schema) {
        List<Bean> beans = admin.list(schema);
        JaxrsConfigBeans result = new JaxrsConfigBeans();
        if (beans.isEmpty()) {
            return result;
        }
        if (beans.size() == 0) {
            return result;
        }
        for (Bean bean : beans) {
            result.addBean(bean);
        }
        result.setTotalCount(beans.size());
        return result;
    }

    @GET
    @Path("paginatebeans/{schema}")
    @Produces({ APPLICATION_JSON })
    public JaxrsConfigBeans paginatebeans(@PathParam("schema") final String schema,
            @QueryParam("first") int first, @QueryParam("max") int max) {
        List<Bean> beans = admin.newQuery(schema)
                .setFirstResult(first)
                .setMaxResults(max)
                .retrieve();
        JaxrsConfigBeans result = new JaxrsConfigBeans();
        if (beans.isEmpty()) {
            return result;
        }
        if (beans.size() == 0) {
            return result;
        }
        for (Bean bean : beans) {
            result.addBean(bean);
        }
        return result;
    }

    @POST
    @Consumes({ APPLICATION_JSON })
    @Path("setbean")
    public void setbean(final JaxrsConfigBean bean) {
        Bean setbean = jaxrsToBean(bean);
        admin.set(setbean);
    }

    @DELETE
    @Path("deletebean/{schemaName}/{id}")
    @Consumes({ APPLICATION_XML })
    public void deletebean(@PathParam("schemaName") final String schema,
            @PathParam("id") final String id) {
        admin.delete(BeanId.create(id, schema));
    }

    private Bean jaxrsToBean(final JaxrsConfigBean bean) {
        Schema schema = admin.getSchemas().get(bean.getSchemaName());
        final BeanId id;
        if (schema.getId().isSingleton()) {
            id = BeanId.createSingleton(schema.getName());
        } else {
            id = BeanId.create(bean.getId(), bean.getSchemaName());
        }
        Bean setbean = Bean.create(id);
        Map<String, List<String>> props = bean.getProperties();
        for (String name : schema.getPropertyNames()) {
            List<String> values = props.get(name);
            if (values == null) {
                continue;
            }
            setbean.addProperty(name, values);
        }
        for (String name : schema.getReferenceNames()) {
            List<String> values = props.get(name);
            if (values == null) {
                continue;
            }

            SchemaPropertyRef ref = schema.get(SchemaPropertyRef.class, name);
            String schemaName = null;
            if (ref != null) {
                schemaName = ref.getSchemaName();
            }

            SchemaPropertyRefList refList = schema.get(SchemaPropertyRefList.class, name);
            if (refList != null) {
                schemaName = refList.getSchemaName();
            }
            SchemaPropertyRefMap refMap = schema.get(SchemaPropertyRefMap.class, name);
            if (refMap != null) {
                schemaName = refMap.getSchemaName();
            }

            for (String value : values) {
                setbean.addReference(name, BeanId.create(value, schemaName));
            }
        }
        return setbean;
    }

}
