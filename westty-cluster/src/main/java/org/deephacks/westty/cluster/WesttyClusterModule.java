package org.deephacks.westty.cluster;

import javax.inject.Inject;

import org.deephacks.westty.properties.WesttyProperties;
import org.deephacks.westty.spi.WesttyModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class WesttyClusterModule implements WesttyModule {
    private static final Logger log = LoggerFactory.getLogger(WesttyClusterModule.class);

    private final WesttyClusterPropertis clusterProperties;
    private HazelcastInstance hazlecast;

    @Inject
    public WesttyClusterModule(WesttyProperties properties) {
        this.clusterProperties = new WesttyClusterPropertis(properties);
    }

    @Override
    public int priority() {
        return 3000;
    }

    @Override
    public synchronized void startup() {
        Config cfg = clusterProperties.getConfig();
        this.hazlecast = Hazelcast.newHazelcastInstance(cfg);
    }

    @Override
    public void shutdown() {
        Hazelcast.shutdownAll();
    }
}
