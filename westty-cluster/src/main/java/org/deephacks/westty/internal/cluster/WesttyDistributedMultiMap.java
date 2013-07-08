package org.deephacks.westty.internal.cluster;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MultiMap;
import org.deephacks.westty.cluster.ClusterListener;
import org.deephacks.westty.cluster.DistributedMultiMap;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

class WesttyDistributedMultiMap<K, V> implements DistributedMultiMap<K, V> {
    private MultiMap<K, V> map;

    public WesttyDistributedMultiMap(MultiMap<K, V> map) {
        this.map = map;
    }

    @Override
    public boolean remove(Object key, Object value) {
        return map.remove(key, value);
    }

    @Override
    public boolean put(K key, V value) {
        return map.put(key, value);
    }

    @Override
    public void addEntryListener(final ClusterListener<K, V> listener, boolean includeValue) {
        EntryListener<K, V> delegate = new EntryListener<K, V>() {

            @Override
            public void entryAdded(EntryEvent<K, V> event) {
                listener.entryAdded(new WesttyEntryEvent<>(event));
            }

            @Override
            public void entryRemoved(EntryEvent<K, V> event) {
                listener.entryRemoved(new WesttyEntryEvent<>(event));
            }

            @Override
            public void entryUpdated(EntryEvent<K, V> event) {
                listener.entryUpdated(new WesttyEntryEvent<>(event));
            }

            @Override
            public void entryEvicted(EntryEvent<K, V> event) {
                listener.entryEvicted(new WesttyEntryEvent<>(event));
            }
        };
        map.addEntryListener(delegate, includeValue);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    @Override
    public Collection<V> get(K key) {
        return map.get(key);
    }
}
