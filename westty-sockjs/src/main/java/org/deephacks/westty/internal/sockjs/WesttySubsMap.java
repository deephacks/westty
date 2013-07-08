package org.deephacks.westty.internal.sockjs;

import org.deephacks.westty.cluster.ClusterListener;
import org.deephacks.westty.cluster.DistributedMultiMap;
import org.deephacks.westty.cluster.EntryEvent;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.eventbus.impl.ServerIDs;
import org.vertx.java.core.eventbus.impl.SubsMap;
import org.vertx.java.core.eventbus.impl.hazelcast.HazelcastServerID;
import org.vertx.java.core.impl.BlockingAction;
import org.vertx.java.core.impl.VertxInternal;
import org.vertx.java.core.net.impl.ServerID;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Modified version of HazelcastSubsMap by <a href="http://tfox.org">Tim Fox</a>.
 *
 * Purpose is to decouple hazelcast implementation from Vertx.
 */
public class WesttySubsMap implements SubsMap, ClusterListener<String, HazelcastServerID> {
    private final VertxInternal vertx;
    private final DistributedMultiMap<String, HazelcastServerID> map;

    public WesttySubsMap(String name, VertxInternal vertx,
            DistributedMultiMap<String, HazelcastServerID> map) {
        this.vertx = vertx;
        this.map = map;
        map.addEntryListener(this, true);
    }

    /*
     The Hazelcast near cache is very slow so we use our own one.
     Keeping it in sync is a little tricky. As entries are added or removed the EntryListener will be called
     but when the node joins the cluster it isn't provided the initial state via the EntryListener
     Therefore the first time get is called for a subscription we *always* get the subs from
     Hazelcast (this is what the initialised flag is for), then consider that the initial state.
     While the get is in progress the entry listener may be being called, so we merge any
     pre-existing entries so we don't lose any. Hazelcast doesn't seem to have any consistent
     way to get an initial state plus a stream of updates.
      */
    private ConcurrentMap<String, ServerIDs> cache = new ConcurrentHashMap<>();

    public void removeAllForServerID(final ServerID serverID,
            final AsyncResultHandler<Void> completionHandler) {
        new BlockingAction<Void>(vertx, completionHandler) {
            public Void action() throws Exception {
                for (Map.Entry<String, HazelcastServerID> entry : map.entrySet()) {
                    HazelcastServerID hid = entry.getValue();
                    if (hid.serverID.equals(serverID)) {
                        map.remove(entry.getKey(), hid);
                    }
                }
                return null;
            }
        }.run();
    }

    @Override
    public void put(final String subName, final ServerID serverID,
            final AsyncResultHandler<Void> completionHandler) {
        new BlockingAction<Void>(vertx, completionHandler) {
            public Void action() throws Exception {
                map.put(subName, new HazelcastServerID(serverID));
                return null;
            }
        }.run();
    }

    @Override
    public void get(final String subName, final AsyncResultHandler<ServerIDs> completionHandler) {
        ServerIDs entries = cache.get(subName);
        if (entries != null && entries.isInitialised()) {
            completionHandler.handle(new AsyncResult<>(entries));
        } else {
            new BlockingAction<Collection<HazelcastServerID>>(vertx,
                    new AsyncResultHandler<Collection<HazelcastServerID>>() {
                        public void handle(AsyncResult<Collection<HazelcastServerID>> result) {
                            AsyncResult<ServerIDs> sresult;
                            if (result.succeeded()) {
                                Collection<HazelcastServerID> entries = result.result;
                                ServerIDs sids;
                                if (entries != null) {
                                    sids = new ServerIDs(entries.size());
                                    for (HazelcastServerID hid : entries) {
                                        sids.add(hid.serverID);
                                    }
                                } else {
                                    sids = new ServerIDs(0);
                                }
                                ServerIDs prev = cache.putIfAbsent(subName, sids);
                                if (prev != null) {
                                    // Merge them
                                    prev.merge(sids);
                                    sids = prev;
                                }
                                sids.setInitialised();
                                sresult = new AsyncResult<>(sids);
                            } else {
                                sresult = new AsyncResult<>(result.exception);
                            }
                            completionHandler.handle(sresult);
                        }
                    }) {
                public Collection<HazelcastServerID> action() throws Exception {
                    return map.get(subName);
                }
            }.run();
        }
    }

    @Override
    public void remove(final String subName, final ServerID serverID,
            final AsyncResultHandler<Boolean> completionHandler) {
        new BlockingAction<Boolean>(vertx, completionHandler) {
            public Boolean action() throws Exception {
                return map.remove(subName, new HazelcastServerID(serverID));
            }
        }.run();
    }

    @Override
    public void entryAdded(EntryEvent<String, HazelcastServerID> entry) {
        addEntry(entry.getKey(), entry.getValue().serverID);
    }

    private void addEntry(String key, ServerID value) {
        ServerIDs entries = cache.get(key);
        if (entries == null) {
            entries = new ServerIDs(1);
            ServerIDs prev = cache.putIfAbsent(key, entries);
            if (prev != null) {
                entries = prev;
            }
        }
        entries.add(value);
    }

    @Override
    public void entryRemoved(EntryEvent<String, HazelcastServerID> entry) {
        removeEntry(entry.getKey(), entry.getValue().serverID);
    }

    private void removeEntry(String key, ServerID value) {
        ServerIDs entries = cache.get(key);
        if (entries != null) {
            entries.remove(value);
            if (entries.isEmpty()) {
                cache.remove(key);
            }
        }
    }

    @Override
    public void entryUpdated(EntryEvent<String, HazelcastServerID> entry) {
        String key = entry.getKey();
        ServerIDs entries = cache.get(key);
        if (entries != null) {
            entries.add(entry.getValue().serverID);
        }
    }

    @Override
    public void entryEvicted(EntryEvent<String, HazelcastServerID> entry) {
        entryRemoved(entry);
    }
}
