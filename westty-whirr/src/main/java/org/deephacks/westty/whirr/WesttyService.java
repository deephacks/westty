package org.deephacks.westty.whirr;

import static org.apache.whirr.RolePredicates.role;
import static org.jclouds.scriptbuilder.domain.Statements.call;

import java.io.IOException;

import org.apache.commons.configuration.Configuration;
import org.apache.whirr.Cluster;
import org.apache.whirr.Cluster.Instance;
import org.apache.whirr.service.ClusterActionEvent;
import org.apache.whirr.service.ClusterActionHandlerSupport;
import org.apache.whirr.service.FirewallManager.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Westty cluster action handler which configures westty by unpacking a binary tarball and
 * setting WESTTY_HOME and PATH environment variables.
 */
public class WesttyService extends ClusterActionHandlerSupport {
    private static final Logger LOG = LoggerFactory.getLogger(WesttyService.class);
    public final static String WESTTY_ROLE = "westty";

    final static String WESTTY_DEFAULT_PROPERTIES = "whirr-westty-default.properties";
    final static int WESTTY_PORT = 80;

    final static String WESTTY_TAR_URL = "whirr.westty.tarball.url";

    final static String WESTTY_SCRIPT = "configure_westty";

    final static String URL_FLAG = "-u";

    @Override
    public String getRole() {
        return WESTTY_ROLE;
    }

    @Override
    protected void beforeConfigure(ClusterActionEvent event) throws IOException {
        Cluster cluster = event.getCluster();
        Instance instance = cluster.getInstanceMatching(role(WESTTY_ROLE));
        event.getFirewallManager().addRules(Rule.create().destination(instance).ports(WESTTY_PORT));

        String ip = instance.getPublicIp();

        addStatement(event, call("retry_helpers"));
        LOG.info("Ip address " + ip);
        addStatement(event, call("install_nginx", ip));
    }

    @Override
    protected void beforeBootstrap(ClusterActionEvent event) throws IOException,
            InterruptedException {

        Configuration conf = getConfiguration(event.getClusterSpec(), WESTTY_DEFAULT_PROPERTIES);

        addStatement(event, call("retry_helpers"));
        addStatement(event, call("install_tarball"));
        addStatement(event, call("install_jre7"));

        String tarball = prepareRemoteFileUrl(event, conf.getString(WESTTY_TAR_URL));

        addStatement(event, call(WESTTY_SCRIPT, URL_FLAG, tarball));
    }

    @Override
    protected void beforeStart(ClusterActionEvent event) throws IOException {

        addStatement(event, call("start_nginx"));
    }
}