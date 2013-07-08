package org.deephacks.westty.tests;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import org.deephacks.westty.config.ServerConfig;

import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class JaxrsClient {

    private final String address;

    public JaxrsClient() {
        this(ServerConfig.DEFAULT_IP_ADDRESS, ServerConfig.DEFAULT_HTTP_PORT);
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
                if (conn.getResponseCode() >= Status.BAD_REQUEST.getStatusCode()) {
                    String body = CharStreams.toString(new InputStreamReader(conn.getErrorStream()));
                    throw new RuntimeException(body);
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
