package org.deephacks.westty.datasource;

import javax.enterprise.inject.Alternative;

import org.deephacks.westty.properties.WesttyProperties;

@Alternative
public class DataSourceProperties extends WesttyProperties {
    public static final String PASSWORD = "westty.datasource.password";
    public static final String USER = "westty.datasource.username";
    public static final String DRIVER = "westty.datasource.driver";
    public static final String URL = "westty.datasource.url";

    public DataSourceProperties() {
        setPropertyIfNotSet(USER, "westty");
        setPropertyIfNotSet(PASSWORD, "westty");
        setPropertyIfNotSet(URL, "jdbc:derby:memory:westty;create=true");
        setPropertyIfNotSet(DRIVER, "org.apache.derby.jdbc.EmbeddedDriver");
    }

    public String getUsername() {
        return getProperty(USER);
    }

    public void setUsername(String username) {
        setProperty(USER, username);
    }

    public String getPassword() {
        return getProperty(PASSWORD);
    }

    public void setPassword(String password) {
        setProperty(PASSWORD, password);
    }

    public String getUrl() {
        return getProperty(URL);
    }

    public void setUrl(String url) {
        setProperty(URL, url);
    }

    public String getDriver() {
        return getProperty(DRIVER);
    }

    public void setDriver(String driver) {
        setProperty(DRIVER, driver);
    }

}
