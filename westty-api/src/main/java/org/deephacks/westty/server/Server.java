package org.deephacks.westty.server;

import java.io.Serializable;

public class Server implements Serializable {

    private int port;
    private String host;

    public Server(String host, int port) {
        this.port = port;
        this.host = host;
    }

    public Server() {
    }

    public int getPort(){
        return port;
    }

    public String getHost(){
        return host;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Server server = (Server) o;

        if (port != server.port)
            return false;
        if (!host.equals(server.host))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = port;
        result = 31 * result + host.hashCode();
        return result;
    }

    public String toString() {
        return host + ":" + port;
    }

}
