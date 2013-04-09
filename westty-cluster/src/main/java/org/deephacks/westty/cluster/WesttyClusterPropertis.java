package org.deephacks.westty.cluster;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.enterprise.inject.Alternative;

import org.deephacks.westty.properties.WesttyProperties;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.hazelcast.config.Config;
import com.hazelcast.config.Join;
import com.hazelcast.config.NetworkConfig;

@Alternative
public class WesttyClusterPropertis extends WesttyProperties {

    public static final String CLUSTER_IPS = "westty.cluster.ips";

    public WesttyClusterPropertis(WesttyProperties properties) {
        super(properties);
    }

    public String getClusterIps() {
        return getProperty(CLUSTER_IPS);
    }

    public void setClusterIps(String ips) {
        setProperty(CLUSTER_IPS, ips);
    }

    Config getConfig() {
        Config cfg = new Config();
        NetworkConfig network = cfg.getNetworkConfig();
        network.setPort(NetworkConfig.DEFAULT_PORT);
        network.setPortAutoIncrement(false);

        Join join = network.getJoin();
        join.getMulticastConfig().setEnabled(false);
        List<String> ips = new ArrayList<>();
        String ip = null;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            ip = "0.0.0.0";
        }

        ips.addAll(getClusterIps(ip));
        for (String anIp : ips) {
            join.getTcpIpConfig().addMember(anIp).setEnabled(true);
        }
        network.getInterfaces().setEnabled(true).addInterface(ip);
        return cfg;
    }

    private Collection<String> getClusterIps(String ip) {
        if (Strings.isNullOrEmpty(getClusterIps())) {
            return ImmutableList.of(ip);
        }
        Collection<String> ips = new HashSet<>();
        for (String anIp : getClusterIps().split(",")) {
            ips.add(anIp.trim());
        }
        return ips;
    }
}