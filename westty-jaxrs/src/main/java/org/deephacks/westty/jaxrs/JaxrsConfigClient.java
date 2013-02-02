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

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.VisibilityChecker;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.MapType;
import org.deephacks.tools4j.config.model.AbortRuntimeException;
import org.deephacks.tools4j.config.model.Event;
import org.deephacks.tools4j.config.model.Schema;

import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;

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

    public JaxrsConfigClient(String host, int port) {
        this.address = "http://" + host + ":" + port + PATH;
    }

    public Map<String, Schema> getschema() throws AbortRuntimeException {
        String response = gethttp("/");
        try {
            MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class,
                    Schema.class);
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
     * @param schema class of the configurable object to be removed
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
     * List all beans of a particular type.
     * 
     * @param schema class of the configurable object to be removed 
     * @return List of configurable objects.
     * @throws AbortRuntimeException See AdminContext for more information.
     */
    public <T> List<T> list(Class<T> schema) throws AbortRuntimeException {
        String response = gethttp("/list/" + schema.getName());
        try {
            CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class,
                    schema);
            return mapper.readValue(response, type);
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
                JaxrsConfigError err = null;
                try {
                    err = mapper.readValue(body, JaxrsConfigError.class);
                } catch (Exception e) {
                    throw new RuntimeException("HTTP error code " + conn.getResponseCode() + ". "
                            + conn.getResponseMessage() + ". " + body);
                }
                if (err != null && err.getCode() > 0) {
                    throw new AbortRuntimeException(new Event(err.getModule(), err.getCode(),
                            err.getMessage()));
                } else if (err != null) {
                    throw new RuntimeException(err.getMessage());
                } else {
                    throw new RuntimeException("HTTP error code " + conn.getResponseCode() + ". "
                            + conn.getResponseMessage() + ". " + body);
                }
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
}
