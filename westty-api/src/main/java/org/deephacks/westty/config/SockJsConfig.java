package org.deephacks.westty.config;

import com.google.common.base.Strings;
import org.deephacks.tools4j.config.Config;
import org.deephacks.tools4j.config.ConfigScope;
import org.deephacks.tools4j.config.Id;

import static org.deephacks.westty.WesttyProperties.getProperty;
import static org.deephacks.westty.WesttyProperties.setProperty;

@ConfigScope
@Config(name="westty.sockjs", desc="SockJS configuration. Changes requires restart.")
public class SockJsConfig {

    public static final String HTTP_PORT_PROP = "westty.sockjs_http.port";
    public static final String EVENTBUS_PORT_PROP = "westty.sockjs_eventbus.port";

    public static final Integer DEFAULT_HTTP_PORT = 8090;
    public static final Integer DEFAULT_EVENTBUS_PORT = 2550;

    @Id(desc="Name of this server")
    private String serverName = ServerConfig.DEFAULT_SERVER_NAME;

    @Config(desc="Http port for SockJS server.")
    private Integer httpPort;

    @Config(desc="Port for SockJS eventbus.")
    private Integer eventBusPort;

    public SockJsConfig(){

    }

    public SockJsConfig(String serverName){
        this.serverName = serverName;
    }

    public Integer getHttpPort() {
        if(httpPort != null){
            return httpPort;
        }
        String value = getProperty(HTTP_PORT_PROP);
        if (!Strings.isNullOrEmpty(value)) {
            return Integer.parseInt(value);
        }
        return DEFAULT_HTTP_PORT;
    }

    public static void setHttpPortProperty(Integer port) {
        setProperty(HTTP_PORT_PROP, port.toString());
    }

    public void setHttpPort(Integer port) {
        this.httpPort = port;
    }

    public static void setEventBusPortProperty(Integer port) {
        setProperty(EVENTBUS_PORT_PROP, port.toString());
    }

    public void setEventBusPort(Integer port) {
        eventBusPort = port;
    }

    public int getEventBusPort() {
        if(eventBusPort != null){
            return eventBusPort;
        }
        String value = getProperty(EVENTBUS_PORT_PROP);
        if (!Strings.isNullOrEmpty(value)) {
            return Integer.parseInt(value);
        }
        return DEFAULT_EVENTBUS_PORT;

    }
}
