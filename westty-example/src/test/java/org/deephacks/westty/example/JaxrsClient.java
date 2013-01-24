package org.deephacks.westty.example;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.VisibilityChecker;
import org.deephacks.tools4j.config.internal.admin.jaxrs.JaxrsConfigError;
import org.deephacks.tools4j.config.model.AbortRuntimeException;
import org.deephacks.tools4j.config.model.Event;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;

public class JaxrsClient {

    private final String address;
    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(
                JsonAutoDetect.Visibility.ANY));
    }

    public JaxrsClient(String host, int port) {
        this.address = "http://" + host + ":" + port;
    }

    public String gethttp(String path) {
        return gethttp(path, null);
    }

    public String gethttp(String path, Object o) {
        return request("GET", path, o);
    }

    public String deletehttp(String path) {
        return request("DELETE", path, null);
    }

    public String postHttpForm(String path, FormParam... params) {
        String content = FormParam.encode(Arrays.asList(params));
        return posthttp(path, content, APPLICATION_FORM_URLENCODED);
    }

    public String postHttpForm(String path, Collection<FormParam> params) {
        String content = FormParam.encode(params);
        return posthttp(path, content, APPLICATION_FORM_URLENCODED);
    }

    public String posthttp(String path, Object o, String encoding) {
        return request("POST", path, o, encoding);
    }

    public String posthttp(String path, Object o) {
        return request("POST", path, o, APPLICATION_JSON);
    }

    private String request(String method, String path, Object o) {
        return request(method, path, o, APPLICATION_JSON);
    }

    private String request(String method, String path, Object o, String contentType) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(address + path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setRequestProperty(CONTENT_TYPE, contentType);
            if (o != null) {
                OutputStream os = null;
                try {
                    conn.setDoOutput(true);
                    os = conn.getOutputStream();
                    if (o instanceof String) {
                        os.write(o.toString().getBytes(Charsets.UTF_8));
                    } else {
                        mapper.writeValue(os, o);
                    }

                    os.flush();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    Closeables.closeQuietly(os);
                    conn.disconnect();
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

    public static class FormParam {
        private String name;
        private String value;

        public FormParam(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public static String encode(Collection<FormParam> params) {
            Iterator<FormParam> it = params.iterator();
            StringBuilder sb = new StringBuilder();
            while (it.hasNext()) {
                FormParam param = it.next();
                try {
                    String encoded = URLEncoder.encode(param.name, "UTF-8") + "="
                            + URLEncoder.encode(param.value, "UTF-8");
                    sb.append(encoded);
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
                if (it.hasNext()) {
                    sb.append('&');
                }
            }
            return sb.toString();
        }

    }

}
