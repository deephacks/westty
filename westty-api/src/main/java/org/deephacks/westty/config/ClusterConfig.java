package org.deephacks.westty.config;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import org.deephacks.tools4j.config.Config;
import org.deephacks.tools4j.config.ConfigScope;

import java.util.ArrayList;
import java.util.List;

import static org.deephacks.westty.WesttyProperties.getProperty;
import static org.deephacks.westty.WesttyProperties.setProperty;

@ConfigScope
@Config(name = "westty.cluster",
        desc = "Server cluster configuration. Changes requires server restart.")
public class ClusterConfig {

    public static final String CLUSTER_SERVERS_PROP = "westty.cluster.servers";

    @Config(desc="Server that are configured as members of this cluster.")
    private List<ServerConfig> servers = new ArrayList<>();

    public List<ServerConfig> getServers(ServerConfig thisServer) {
        if(servers != null && servers.size() != 0){
            return servers;
        }

        String prop = getProperty(CLUSTER_SERVERS_PROP);
        if (Strings.isNullOrEmpty(prop)) {
            return ImmutableList.of(thisServer);
        }
        List<ServerConfig> serverList = new ArrayList<>();
        for (String serverName : prop.split(",")) {
            serverList.add(new ServerConfig(serverName));
        }
        return serverList;
    }

    public void setServers(List<ServerConfig> servers) {
        this.servers = servers;
    }

    public void setServerNames(String... servers) {
        this.servers = new ArrayList<>();
        for (String name : servers) {
            this.servers.add(new ServerConfig(name));
        }
    }

    public static void setServersProperty(List<String> names) {
        StringBuilder sb = new StringBuilder();
        String[] list = names.toArray(new String[0]);
        for (int i = 0; i < list.length; i++) {
            sb.append(list[i]);
            if ((i + 1) < list.length) {
                sb.append(",");
            }
        }
        setProperty(CLUSTER_SERVERS_PROP, sb.toString());
    }


}
