package org.deephacks.westty.cluster;

public interface EntryEvent<K, V> {
    /**
     * Returns the key of the entry event
     *
     * @return the key
     */
    public K getKey();

    /**
     * Returns the old value of the entry event
     *
     * @return
     */
    public V getOldValue();

    /**
     * Returns the value of the entry event
     *
     * @return
     */
    public V getValue();

    /**
     * Returns the member fired this event.
     *
     * @return the member fired this event.
     */
    public WesttyServerId getMember();

    /**
     * Return the event type
     *
     * @return event type
     */
    public EntryEventType getEventType();

    /**
     * Returns the name of the map for this event.
     *
     * @return name of the map.
     */
    public String getName();

    public static enum EntryEventType {
        ADDED(1), REMOVED(2), UPDATED(3), EVICTED(4);

        private int type;

        private EntryEventType(final int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }

        public static EntryEventType getByType(final int eventType) {
            for (EntryEventType entryEventType : values()) {
                if (entryEventType.type == eventType) {
                    return entryEventType;
                }
            }
            return null;
        }
    }

}
