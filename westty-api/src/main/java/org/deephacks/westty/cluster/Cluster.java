package org.deephacks.westty.cluster;

import org.deephacks.westty.server.Server;

import java.util.Set;

public interface Cluster {

    public Set<Server> getMembers();

    public <K, V> DistributedMultiMap<K, V> getMultiMap(String name);

}
