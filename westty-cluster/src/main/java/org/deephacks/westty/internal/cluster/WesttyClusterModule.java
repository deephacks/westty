package org.deephacks.westty.internal.cluster;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.deephacks.westty.cluster.DistributedMultiMap;
import org.deephacks.westty.cluster.WesttyCluster;
import org.deephacks.westty.cluster.WesttyClusterProperties;
import org.deephacks.westty.cluster.WesttyServerId;
import org.deephacks.westty.properties.WesttyProperties;
import org.deephacks.westty.spi.WesttyModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.MultiMap;

@Singleton
public class WesttyClusterModule implements WesttyModule, WesttyCluster {
    private static final Logger log = LoggerFactory.getLogger(WesttyClusterModule.class);
    private static final String CLUSTER_MEMBERS_KEY = "cluster_members";
    private final WesttyClusterProperties props;
    private HazelcastInstance hazelcast;
    private WesttyServerId id;

    @Inject
    public WesttyClusterModule(WesttyProperties properties) {
        this.props = new WesttyClusterProperties(properties);
        Config cfg = props.getConfig();
        this.hazelcast = Hazelcast.newHazelcastInstance(cfg);
        this.id = new WesttyServerId(props.getPrivateIp(), props.getClusterPort());
        log.info("Cluster members {}", getMembers());
    }

    @Override
    public int priority() {
        return 500;
    }

    @Override
    public synchronized void startup() {

    }

    @Override
    public void shutdown() {
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
