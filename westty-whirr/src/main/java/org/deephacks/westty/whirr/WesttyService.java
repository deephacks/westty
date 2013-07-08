package org.deephacks.westty.whirr;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.configuration.Configuration;
import org.apache.whirr.Cluster;
import org.apache.whirr.Cluster.Instance;
import org.apache.whirr.service.ClusterActionEvent;
import org.apache.whirr.service.ClusterActionHandlerSupport;
import org.apache.whirr.service.FirewallManager.Rule;
import org.deephacks.westty.config.ClusterConfig;
import org.deephacks.westty.config.ProtobufConfig;
import org.deephacks.westty.config.ServerConfig;
import org.deephacks.westty.config.SockJsConfig;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.apache.whirr.RolePredicates.role;
import static org.jclouds.scriptbuilder.domain.Statements.call;

/**
 * Westty cluster action handler which configures westty by unpacking a binary tarball and
 * setting WESTTY_HOME and PATH environment variables.
 */
public class WesttyService extends ClusterActionHandlerSupport {

    public final static String WESTTY_ROLE = "westty";

    final static String WESTTY_DEFAULT_PROPERTIES = "whirr-westty-default.properties";
    final static int WESTTY_HTTP_PORT = 80;

    final static String WESTTY_TAR_URL = "whirr.westty.tarball.url";

    final static String URL_FLAG = "-u";

    @Override
    public String getRole() {
        return WESTTY_ROLE;
    }

    @Override
    protected void beforeConfigure(ClusterActionEvent event) throws IOException {
        Cluster cluster = event.getCluster();

        Set<Instance> instances = cluster.getInstancesMatching(role(WESTTY_ROLE));

        event.getStatementBuilder().addExport(ClusterConfig.CLUSTER_SERVERS_PROP, generateClusterIds(instances));

        event.getFirewallManager().addRules(
                Rule.create().destination(instances).ports(WESTTY_HTTP_PORT));
        event.getFirewallManager().addRules(
                Rule.create().destination(instances).ports(SockJsConfig.DEFAULT_HTTP_PORT));
        event.getFirewallManager().addRules(
                Rule.create().destination(instances).ports(ProtobufConfig.DEFAULT_PORT));
        //        for (Instance instance : instances) {
        //            String ip = instance.getPublicIp();
        //            event.getStatementBuilder()
        //                    .addExportPerInstance(instance.getId(), WESTTY_PUBLIC_IP, ip);
        //        }
        handleFirewallRules(event);

        addStatement(event, call("retry_helpers"));
        addStatement(event, call("install_westty"));
        addStatement(event, call("install_nginx"));
    }

    @Override
    protected void beforeBootstrap(ClusterActionEvent event) throws IOException,
            InterruptedException {

        Configuration conf = getConfiguration(event.getClusterSpec(), WESTTY_DEFAULT_PROPERTIES);

        addStatement(event, call("retry_helpers"));
        addStatement(event, call("install_tarball"));
        addStatement(event, call("install_jre7"));

        String tarball = prepareRemoteFileUrl(event, conf.getString(WESTTY_TAR_URL));

        addStatement(event, call("configure_westty", URL_FLAG, tarball));
    }

    @Override
    protected void beforeStart(ClusterActionEvent event) throws IOException {
        addStatement(event, call("start_nginx"));
    }

    private String generateClusterIds(Set<Instance> instances) {
        return Joiner.on(',').join(getPrivateHosts(instances));
    }

    private static List<String> getPrivateHosts(Set<Instance> instances) {
        return Lists.transform(Lists.newArrayList(instances), new Function<Instance, String>() {
            @Override
            public String apply(Instance instance) {
                String host = instance.getPrivateIp();
                return String.format("%s:%d", host, ServerConfig.DEFAULT_CLUSTER_PORT);
            }
        });
    }
}