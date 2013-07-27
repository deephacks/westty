package org.deephacks.westty.config;

import org.deephacks.confit.Config;

@Config(name="datasource")
public class DataSourceConfig {

    private String password = "westty";
    private String user = "westty";
    private String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    private String url  ="jdbc:derby:memory:westty;create=true";

    public DataSourceConfig() {
    }

    public String getPassword() {
        return password;
    }

    public String getUser() {
        return user;
    }

    public String getDriver() {
        return driver;
    }

    public String getUrl() {
        return url;
    }
}
