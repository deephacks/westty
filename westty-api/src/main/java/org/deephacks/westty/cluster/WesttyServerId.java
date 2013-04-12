package org.deephacks.westty.cluster;

import java.io.Serializable;

public class WesttyServerId implements Serializable {

    public int port;
    public String host;

    public WesttyServerId(String host, int port) {
        this.port = port;
        this.host = host;
    }

    public WesttyServerId() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        WesttyServerId serverID = (WesttyServerId) o;

        if (port != serverID.port)
            return false;
        if (!host.equals(serverID.host))
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
