package org.deephacks.westty.cluster;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import org.deephacks.westty.WesttyProperties;
import org.deephacks.westty.config.ClusterConfig;
import org.deephacks.westty.server.Server;

import javax.enterprise.inject.Alternative;
import java.util.Collection;
import java.util.HashSet;

@Alternative
public class ClusterProperties extends WesttyProperties {

    public static final String SERVERS = "westty.cluster.servers";
    public static final String CLUSTER_PORT = "westty.cluster.port";

}