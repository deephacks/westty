package org.deephacks.westty.internal.cluster;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.inject.Singleton;

import org.deephacks.westty.cluster.DistributedMultiMap;
import org.deephacks.westty.cluster.WesttyCluster;
import org.deephacks.westty.cluster.WesttyClusterProperties;
import org.deephacks.westty.cluster.WesttyServerId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.MultiMap;

@Singleton
public class ClusterBootstrap implements WesttyCluster {
    private static final Logger log = LoggerFactory.getLogger(ClusterBootstrap.class);

    private final WesttyClusterProperties props;
    private HazelcastInstance hazelcast;

    static {
        System.setProperty("hazelcast.logging.type", "slf4j");
    }

    public ClusterBootstrap() {
        this.props = new WesttyClusterProperties();
        Config cfg = props.getConfig();
        this.hazelcast = Hazelcast.newHazelcastInstance(cfg);
        log.info("Cluster members {}", getMembers());

    }

    public void shutdown(@Observes BeforeShutdown event) {
        Hazelcast.shutdownAll();
    }

    @Override
    public <K, V> DistributedMultiMap<K, V> getMultiMap(String name) {
        MultiMap<K, V> map = hazelcast.getMultiMap(name);
        return (DistributedMultiMap<K, V>) new WesttyDistributedMultiMap<>(map);
    }

    @Override
    public Set<WesttyServerId> getMembers() {
        Set<WesttyServerId> ids = new HashSet<>();
        for (Member member : hazelcast.getCluster().getMembers()) {
            InetSocketAddress add = member.getInetSocketAddress();
            ids.add(new WesttyServerId(add.getAddress().getHostAddress(), add.getPort()));
        }
        return ids;
    }

}
