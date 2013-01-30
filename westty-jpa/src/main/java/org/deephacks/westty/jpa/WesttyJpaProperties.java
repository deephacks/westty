package org.deephacks.westty.jpa;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;

import org.deephacks.westty.WesttyProperties;
import org.deephacks.westty.WesttyPropertyExtension;
import org.deephacks.westty.datasource.WesttyDataSourceProperties;

import com.google.common.io.Closeables;

@Alternative
public class WesttyJpaProperties extends Properties {
    private static final long serialVersionUID = 1667859287794861790L;
    public static final String USER = "javax.persistence.jdbc.user";
    public static final String PASSWORD = "javax.persistence.jdbc.password";
    public static final String URL = "javax.persistence.jdbc.url";
    public static final String DRIVER = "javax.persistence.jdbc.driver";
    public static final String PROVIDER = "javax.persistence.provider";
    public static final String TX_TYPE = "javax.persistence.transactionType";
    public static final String JPA_UNIT = "westty.jpa.unit";

    public WesttyJpaProperties() {
    }

    public WesttyJpaProperties(WesttyProperties properties) {
        Properties source = properties.getProperties();
        for (String key : source.stringPropertyNames()) {
            setProperty(key, source.getProperty(key));
        }
    }

    public String getJpaUnit() {
        return getProperty(JPA_UNIT);
    }

    public void setJpaUnit(String unitName) {
        setProperty(JPA_UNIT, unitName);
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

    public String getProvider() {
        return getProperty(PROVIDER);
    }

    public void setProvider(String provider) {
        setProperty(PROVIDER, provider);
    }

    public String getTxType() {
        return getProperty(TX_TYPE);
    }

    public void setTxType(String txType) {
        setProperty(TX_TYPE, txType);
    }

    @Singleton
    public static class WesttyJpaDefaultProperties implements WesttyPropertyExtension {
        private static final String JPA_PROPERTIES_FILE = "jpa.properties";

        @Override
        public void extendProperties(WesttyProperties properties) {
            WesttyDataSourceProperties ds = new WesttyDataSourceProperties(properties);
            WesttyJpaProperties jpa = new WesttyJpaProperties();
            jpa.setJpaUnit("westty-jpa-unit");
            jpa.setUsername(ds.getUsername());
            jpa.setPassword(ds.getPassword());
            jpa.setUrl(ds.getUrl());
            jpa.setDriver(ds.getDriver());
            jpa.setProvider("org.hibernate.ejb.HibernatePersistence");
            jpa.setTxType("RESOURCE_LOCAL");
            jpa.setProperty("hibernate.hbm2ddl.auto", "validate");
            jpa.setProperty("hibernate.show_sql", "false");
            jpa.setProperty("hibernate.dialect", "org.hibernate.dialect.DerbyDialect");
            properties.add(jpa);
            properties.loadProperties(new File(properties.getConfDir(), JPA_PROPERTIES_FILE));
        }

        @Override
        public int priority() {
            return 10;
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
