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
package org.deephacks.westty.jaxrs;

import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.VisibilityChecker;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.MapType;
import org.deephacks.tools4j.config.model.AbortRuntimeException;
import org.deephacks.westty.config.ServerConfig;

import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Helper class for sending jaxrs config request in a type-safe way. The intention
 * is to use the real configuration classes, not the model.bean class.
 */
public class JaxrsConfigClient {
    public static final String PATH = "/jaxrs/config-admin-jaxrs";
    private final String address;
    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(
                JsonAutoDetect.Visibility.ANY));
    }
    public JaxrsConfigClient() {
        this(ServerConfig.DEFAULT_IP_ADDRESS, ServerConfig.DEFAULT_HTTP_PORT);
    }
    public JaxrsConfigClient(String host, int port) {
        this.address = "http://" + host + ":" + port + PATH;
    }

    public Map<String, JaxrsSchema> getSchemas() throws AbortRuntimeException {
        String response = gethttp("/getschemas");
        try {
            MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class,
                    JaxrsSchema.class);
            return mapper.readValue(response, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a bean. See AdminContext for more information.
     *
     * @param o a configurable object
     * @throws AbortRuntimeException See AdminContext for more information.
     */
    public void create(Object o) throws AbortRuntimeException {
        JaxrsBean bean = new JaxrsBean(o);
        posthttp("/create", bean);
    }

    /**
     * Set a bean. See AdminContext for more information.
     *
     * @param o a configurable object
     * @throws AbortRuntimeException See AdminContext for more information.
     */
    public void set(Object o) throws AbortRuntimeException {
        JaxrsBean bean = new JaxrsBean(o);
        posthttp("/set", bean);
    }

    /**
     * Merge a bean. See AdminContext for more information.
     *
     * @param o a configurable object
     * @throws AbortRuntimeException See AdminContext for more information.
     */
    public void merge(Object o) throws AbortRuntimeException {
        JaxrsBean bean = new JaxrsBean(o);
        posthttp("/merge", bean);
    }

    /**
     * Delete a bean. See AdminContext for more information.
     *
     * @param schema class of the configurable object to be removed
     * @param id instance id of the object.
     * @throws AbortRuntimeException See AdminContext for more information.
     */
    public void delete(Class<?> schema, String id) throws AbortRuntimeException {
        deletehttp("/delete/" + schema.getName() + "/" + id);
    }

    /**
     * Get a bean. See AdminContext for more information.
     *
     * @param schema class of the configurable object to get
     * @param id instance id of the object.
     * @return Configurable object.
     * @throws AbortRuntimeException See AdminContext for more information.
     */
    public <T> T get(Class<T> schema, String id) throws AbortRuntimeException {
        String response = gethttp("/get/" + schema.getName() + "/" + id);
        try {
            return mapper.readValue(response, schema);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a singleton bean. See AdminContext for more information.
     *
     * @param schema class of the configurable object to get.
     * @return Configurable object.
     * @throws AbortRuntimeException See AdminContext for more information.
     */
    public <T> T getSingleton(Class<T> schema) throws AbortRuntimeException {
        String response = gethttp("/getSingleton/" + schema.getName());
        try {
            return mapper.readValue(response, schema);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * List all beans of a particular type.
     *
     * @param schema class of the configurable object to be removed
     * @return List of configurable objects.
     * @throws AbortRuntimeException See AdminContext for more information.
     */
    public <T> List<T> list(Class<T> schema) throws AbortRuntimeException {
        String response = gethttp("/list/" + schema.getName());
        try {
            JsonNode json = mapper.readValue(response, JsonNode.class);
            JsonNode beans = json.get("beans");
            CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class,
                    schema);
            return mapper.readValue(beans.toString(), type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String gethttp(String path) {
        return gethttp(path, null);
    }

    private String gethttp(String path, Object o) {
        return request("GET", path, o);
    }

    private String deletehttp(String path) {
        return request("DELETE", path, null);
    }

    private String posthttp(String path, Object o) {
        return request("POST", path, o);
    }

    private String request(String method, String path, Object o) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(address + path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setRequestProperty(CONTENT_TYPE, APPLICATION_JSON);
            if (o != null) {
                OutputStream os = null;
                try {
                    conn.setDoOutput(true);
                    os = conn.getOutputStream();
                    mapper.writeValue(os, o);
                    os.flush();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    Closeables.closeQuietly(os);
                }
            }

            if (conn.getResponseCode() >= Status.BAD_REQUEST.getStatusCode()) {
                String body = CharStreams.toString(new InputStreamReader(conn.getErrorStream()));
                throw new HttpException(conn.getResponseCode(), body);
            }
            return CharStreams.toString(new InputStreamReader(conn.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
    public static class JaxrsBeanList {
        private int totalCount;
        private Collection<Object> beans = new ArrayList<>();

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public Collection<Object> getBeans() {
            return beans;
        }

        public void setBeans(Collection<Object> beans) {
            this.beans = beans;
        }
    }
    public static class JaxrsBean {
        private String className;
        private Object bean;

        public JaxrsBean() {

        }

        public JaxrsBean(String className, Object bean) {
            this.className = className;
            this.bean = bean;

        }

        public JaxrsBean(Object o) {
            this.className = o.getClass().getCanonicalName();
            this.bean = o;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getClassName() {
            return className;
        }

        public Object getBean() {
            try {
                Class<?> clazz = Class.forName(className);
                return mapper.convertValue(bean, clazz);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void setBean(Object bean) {
            this.bean = bean;
        }
    }

    public static class HttpException extends RuntimeException {
        private int code;

        public HttpException(int code, String msg){
            super(msg);
            this.code = code;
        }

        public int getCode(){
            return code;
        }
    }
}
