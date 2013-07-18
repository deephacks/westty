package org.deephacks.westty.config;

import org.deephacks.tools4j.config.Config;
import org.deephacks.tools4j.config.ConfigScope;
import org.deephacks.tools4j.config.Id;

@ConfigScope
@Config(name="websocket", desc="Websocket configuration. Changes requires restart.")
public class SockJsConfig {

    public static final Integer DEFAULT_HTTP_PORT = 8090;
    public static final Integer DEFAULT_EVENTBUS_PORT = 2550;

    @Id(desc="Name of this server")
    private String serverName = ServerConfig.DEFAULT_SERVER_NAME;

    @Config(desc="Http port for SockJS server.")
    private Integer httpPort = DEFAULT_HTTP_PORT;

    @Config(desc="Port for SockJS eventbus.")
    private Integer eventBusPort = DEFAULT_EVENTBUS_PORT;

    public SockJsConfig(){

    }

    public SockJsConfig(String serverName){
        this.serverName = serverName;
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public int getEventBusPort() {
        return eventBusPort;
    }

    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }

    public void setEventBusPort(Integer eventBusPort) {
        this.eventBusPort = eventBusPort;
    }
}
