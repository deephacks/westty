package org.deephacks.westty.cluster;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashSet;

import javax.enterprise.inject.Alternative;

import org.deephacks.westty.properties.WesttyProperties;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.hazelcast.config.Config;
import com.hazelcast.config.Join;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.nio.Address;

@Alternative
public class WesttyClusterProperties extends WesttyProperties {

    public static final String CLUSTER_IDS = "westty.cluster.ids";
    public static final String CLUSTER_PORT = "westty.cluster.port";
    public static final int CLUSTER_DEFAUL_PORT = NetworkConfig.DEFAULT_PORT;

    public WesttyClusterProperties(WesttyProperties properties) {
        super(properties);
    }

    public Collection<WesttyServerId> getClusterIds() {
        String prop = getProperty(CLUSTER_IDS);
        if (Strings.isNullOrEmpty(prop)) {
            return ImmutableList.of();
        }
        Collection<WesttyServerId> ips = new HashSet<>();
        for (String address : prop.split(",")) {
            String[] split = address.trim().split(":");
            String host = split[0];
            String port = split[1];
            ips.add(new WesttyServerId(host, Integer.parseInt(port)));
        }
        return ips;
    }

    public void setClusterIds(Collection<WesttyServerId> ids) {
        StringBuilder sb = new StringBuilder();
        WesttyServerId[] list = ids.toArray(new WesttyServerId[0]);
        for (int i = 0; i < list.length; i++) {
            sb.append(list[i].host.trim()).append(":");
            sb.append(list[i].port);
            if ((i + 1) < list.length) {
                sb.append(",");
            }
        }
        setProperty(CLUSTER_IDS, sb.toString());
    }

    public int getClusterPort() {
        String value = getProperty(CLUSTER_PORT);
        if (!Strings.isNullOrEmpty(value)) {
            return Integer.parseInt(value);
        }
        return CLUSTER_DEFAUL_PORT;
    }

    public void setClusterPort(int port) {
        setProperty(CLUSTER_PORT, new Integer(port).toString());
    }

    public Config getConfig() {
        Config cfg = new Config();
        cfg.setProperty("hazelcast.logging.type", "slf4j");
        NetworkConfig network = cfg.getNetworkConfig();
        network.setPort(getClusterPort());
        network.setPortAutoIncrement(false);

        Join join = network.getJoin();
        join.getMulticastConfig().setEnabled(false);

        for (WesttyServerId ip : getClusterIds()) {
            try {
                join.getTcpIpConfig().addAddress(new Address(ip.host, ip.port)).setEnabled(true);
            } catch (UnknownHostException e) {
                throw new IllegalStateException(e);
            }
        }
        String ip = getPrivateIp();
        network.getInterfaces().setEnabled(true).addInterface(ip);
        return cfg;
    }
}