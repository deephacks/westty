package org.deephacks.westty.whirr;

import static org.jclouds.scriptbuilder.domain.Statements.call;

import java.io.IOException;

import org.apache.commons.configuration.Configuration;
import org.apache.whirr.service.ClusterActionEvent;
import org.apache.whirr.service.ClusterActionHandlerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Westty cluster action handler which configures westty by unpacking a binary tarball and
 * setting WESTTY_HOME and PATH environment variables.
 */
public class WesttyService extends ClusterActionHandlerSupport {
    private static final Logger LOG = LoggerFactory.getLogger(WesttyService.class);
    public final static String WESTTY_CLIENT_ROLE = "westty";

    final static String WESTTY_DEFAULT_PROPERTIES = "whirr-westty-default.properties";

    final static String WESTTY_TAR_URL = "whirr.westty.tarball.url";

    final static String WESTTY_CLIENT_SCRIPT = "configure_westty";

    final static String URL_FLAG = "-u";

    @Override
    public String getRole() {
        return WESTTY_CLIENT_ROLE;
    }

    @Override
    protected void beforeBootstrap(ClusterActionEvent event) throws IOException,
            InterruptedException {

        Configuration conf = getConfiguration(event.getClusterSpec(), WESTTY_DEFAULT_PROPERTIES);
        // add apt_get_retry functions in order to install openjdk 
        addStatement(event, call("retry_helpers"));
        // add install_tarball which is used by configure_westty.sh
        addStatement(event, call("install_tarball"));
        addStatement(event, call(getInstallFunction(conf, "java", "install_openjdk")));

        String tarball = prepareRemoteFileUrl(event, conf.getString(WESTTY_TAR_URL));

        addStatement(event, call(WESTTY_CLIENT_SCRIPT, URL_FLAG, tarball));
    }
}