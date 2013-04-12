package org.deephacks.westty.cluster;

import java.util.Set;

public interface WesttyCluster {

    public Set<WesttyServerId> getMembers();

    public <K, V> DistributedMultiMap<K, V> getMultiMap(String name);

}
