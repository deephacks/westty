package org.deephacks.westty.internal.cluster;

import com.hazelcast.config.Config;
import com.hazelcast.config.Join;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.MultiMap;
import com.hazelcast.nio.Address;
import org.deephacks.tools4j.config.ConfigContext;
import org.deephacks.westty.cluster.Cluster;
import org.deephacks.westty.cluster.DistributedMultiMap;
import org.deephacks.westty.config.ClusterConfig;
import org.deephacks.westty.config.ServerConfig;
import org.deephacks.westty.config.ServerSpecificConfigProxy;
import org.deephacks.westty.server.Server;
import org.deephacks.westty.spi.ProviderShutdownEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Singleton
class ClusterBootstrap implements Cluster {
    private static final Logger log = LoggerFactory.getLogger(ClusterBootstrap.class);

    private HazelcastInstance hazelcast;
    private ClusterConfig cluster;
    private ServerConfig server;

    static {
        System.setProperty("hazelcast.logging.type", "slf4j");
    }

    @Inject
    public ClusterBootstrap(ConfigContext ctx, ServerSpecificConfigProxy<ServerConfig> server) {
        this.cluster = ctx.get(ClusterConfig.class);
        this.server = server.get();
        Config cfg = getConfig();
        this.hazelcast = Hazelcast.newHazelcastInstance(cfg);
        log.info("Cluster members {}", getMembers());
    }

    public void shutdown(@Observes ProviderShutdownEvent event) {
        log.info("Shutdown Cluster");
        hazelcast.getLifecycleService().shutdown();
    }

    @Override
    public <K, V> DistributedMultiMap<K, V> getMultiMap(String name) {
        MultiMap<K, V> map = hazelcast.getMultiMap(name);
        return new WesttyDistributedMultiMap<>(map);
    }

    @Override
    public Set<Server> getMembers() {
        Set<Server> ids = new HashSet<>();
        Set<Member> members = new HashSet<>();
        if(hazelcast.getLifecycleService().isRunning()){
            members = hazelcast.getCluster().getMembers();
        }
        for (Member member : members) {
            InetSocketAddress add = member.getInetSocketAddress();
            ids.add(new Server(add.getAddress().getHostAddress(), add.getPort()));
        }
        return ids;
    }
    public Config getConfig() {
        Config cfg = new Config();
        cfg.setProperty("hazelcast.logging.type", "slf4j");
        NetworkConfig network = cfg.getNetworkConfig();
        network.setPort(server.getClusterPort());
        network.setPortAutoIncrement(false);

        Join join = network.getJoin();
        join.getMulticastConfig().setEnabled(false);
        List<ServerConfig> servers = cluster.getServers();
        for (ServerConfig server : servers) {
            try {
                join.getTcpIpConfig().addAddress(new Address(server.getPrivateIp(), server.getClusterPort())).setEnabled(true);
            } catch (UnknownHostException e) {
                throw new IllegalStateException(e);
            }
        }
        network.getInterfaces().setEnabled(true).addInterface(server.getPrivateIp());
        return cfg;
    }
}
