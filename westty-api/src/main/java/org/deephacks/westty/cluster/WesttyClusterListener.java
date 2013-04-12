package org.deephacks.westty.cluster;

public interface WesttyClusterListener<K, V> {
    /**
     * Invoked when an entry is added.
     *
     * @param event entry event
     */
    void entryAdded(EntryEvent<K, V> event);

    /**
     * Invoked when an entry is removed.
     *
     * @param event entry event
     */
    void entryRemoved(EntryEvent<K, V> event);

    /**
     * Invoked when an entry is updated.
     *
     * @param event entry event
     */
    void entryUpdated(EntryEvent<K, V> event);

    /**
     * Invoked when an entry is evicted.
     *
     * @param event entry event
     */
    void entryEvicted(EntryEvent<K, V> event);

}
