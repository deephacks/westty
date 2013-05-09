package org.deephacks.westty.internal.sockjs;

import java.util.Iterator;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.deephacks.westty.cluster.DistributedMultiMap;
import org.deephacks.westty.cluster.WesttyCluster;
import org.deephacks.westty.sockjs.WesttySockJsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.impl.ClusterManager;
import org.vertx.java.core.eventbus.impl.DefaultEventBus;
import org.vertx.java.core.eventbus.impl.SubsMap;
import org.vertx.java.core.eventbus.impl.hazelcast.HazelcastServerID;
import org.vertx.java.core.impl.DefaultVertx;
import org.vertx.java.core.impl.VertxInternal;

@Singleton
public class WesttyVertx extends DefaultVertx {
    private static final Logger log = LoggerFactory.getLogger(WesttyVertx.class);
    private WesttyEventBus bus;
    private WesttySockJsProperties props = new WesttySockJsProperties();
    private WesttyCluster cluster;

    @Inject
    private Instance<WesttyCluster> clusterInstance;

    public WesttyVertx() {
        super();
    }

    @Override
    public EventBus eventBus() {
        if (bus != null) {
            return bus;
        }
        Iterator<WesttyCluster> it = clusterInstance.iterator();
        if (it.hasNext()) {
            cluster = it.next();
        }
        if (cluster == null) {
            log.info("Creating standalone vertx.");
            bus = new WesttyEventBus(this);
        } else {
            log.info("Creating cluster vertx on {}:{} with members " + cluster.getMembers(),
                    props.getEventBusHost(), props.getEventBusPort());
            bus = new WesttyEventBus(this, props.getEventBusPort(), props.getEventBusHost());
        }
        return bus;
    }

    public class WesttyEventBus extends DefaultEventBus {

        public WesttyEventBus(WesttyVertx vertx) {
            super(vertx);
        }

        public WesttyEventBus(WesttyVertx vertx, int port, String host) {
            super(vertx, port, host);
        }

        @Override
        protected ClusterManager createClusterManager(VertxInternal vertx) {
            return new WesttyVertxClusterManager(vertx);
        }
    }

    public class WesttyVertxClusterManager implements ClusterManager {
        private final VertxInternal vertx;

        public WesttyVertxClusterManager(VertxInternal vertx) {
            this.vertx = vertx;
        }

        @Override
        public SubsMap getSubsMap(String name) {
            DistributedMultiMap<String, HazelcastServerID> map = cluster.getMultiMap(name);
            return new WesttySubsMap(name, vertx, map);
        }

        @Override
        public void close() {

        }
    }
}
