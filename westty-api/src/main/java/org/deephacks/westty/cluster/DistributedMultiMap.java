package org.deephacks.westty.cluster;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface DistributedMultiMap<K, V> {

    boolean remove(Object key, Object value);

    boolean put(K key, V value);

    void addEntryListener(WesttyClusterListener<K, V> listener, boolean includeValue);

    Set<Map.Entry<K, V>> entrySet();

    Collection<V> get(K key);
}
