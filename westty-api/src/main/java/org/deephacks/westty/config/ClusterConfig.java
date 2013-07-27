package org.deephacks.westty.config;

import org.deephacks.confit.Config;
import org.deephacks.confit.ConfigScope;

import java.util.ArrayList;
import java.util.List;

@ConfigScope
@Config(name = "cluster",
        desc = "Server cluster configuration. Changes requires server restart.")
public class ClusterConfig {

    @Config(desc="Server that are configured as members of this cluster.")
    private List<ServerConfig> servers = new ArrayList<>();

    public List<ServerConfig> getServers() {
       return servers;
    }

    public void setServers(List<ServerConfig> servers) {
        this.servers = servers;
    }

    public void addServer(ServerConfig server) {
        servers.add(server);
    }
}
