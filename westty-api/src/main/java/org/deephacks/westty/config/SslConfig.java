package org.deephacks.westty.config;

import org.deephacks.tools4j.config.Config;
import org.deephacks.tools4j.config.ConfigScope;
import org.deephacks.tools4j.config.Id;

@Config(name = SslConfig.ID, desc = SslConfig.DESC)
@ConfigScope
public class SslConfig {

    static final String DESC = "Westty SSL configuration.";

    @Id(desc = SslConfig.DESC)
    public static final String ID = "westty.ssl";

    @Config(desc = "Set to true to enable SSL.")
    private Boolean sslEnabled = false;

    @Config(desc = "Defines the path to the SSL key store on the "
            + "client that holds the client certificates.")
    private String keyStorePath = "/etc/keystore.jks";

    @Config(desc = "Defines the password for the client certificate key store on the client.")
    private String keyStorePassword = "secret";

    @Config(desc = "Defines the path to the trusted client certificate store on the server.")
    private String trustStorePath = "/etc/truststore.jks";

    @Config(desc = "Defines the password to the trusted client certificate store on the server.")
    private String trustStorePassword = "secret";

    public Boolean getSslEnabled() {
        return sslEnabled;
    }

    public String getKeyStorePath() {
        return keyStorePath;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public String getTrustStorePath() {
        return trustStorePath;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

}
