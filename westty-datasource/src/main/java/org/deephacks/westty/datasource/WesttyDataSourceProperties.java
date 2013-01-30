package org.deephacks.westty.datasource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;

import org.deephacks.westty.WesttyProperties;
import org.deephacks.westty.WesttyPropertyExtension;

import com.google.common.io.Closeables;

@Alternative
public class WesttyDataSourceProperties extends Properties {
    private static final long serialVersionUID = -2155409639047609415L;
    public static final String PASSWORD = "westty.datasource.password";
    public static final String USER = "westty.datasource.username";
    public static final String DRIVER = "westty.datasource.driver";
    public static final String URL = "westty.datasource.url";

    public WesttyDataSourceProperties() {
    }

    public WesttyDataSourceProperties(WesttyProperties properties) {
        Properties source = properties.getProperties();
        for (String key : source.stringPropertyNames()) {
            setProperty(key, source.getProperty(key));
        }
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

    @Singleton
    public static class WesttyDefaultDataSourceProperties implements WesttyPropertyExtension {

        @Override
        public void extendProperties(WesttyProperties properties) {
            WesttyDataSourceProperties ds = new WesttyDataSourceProperties();
            ds.setUsername("admin");
            ds.setPassword("admin");
            ds.setUrl("jdbc:derby:memory:westty;create=true");
            ds.setDriver("org.apache.derby.jdbc.EmbeddedDriver");
            properties.add(ds);
        }

        @Override
        public int priority() {
            return 0;
        }
    }

    public static Properties loadProperties(File file) {
        Properties props = new Properties();
        if (!file.exists()) {
            return props;
        }
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            props.load(in);
            return props;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            Closeables.closeQuietly(in);
        }

    }
}
