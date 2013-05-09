package org.deephacks.westty.jpa;

import java.io.File;

import javax.enterprise.inject.Alternative;

import org.deephacks.westty.datasource.DataSourceProperties;
import org.deephacks.westty.properties.WesttyProperties;

@Alternative
public class WesttyJpaProperties extends DataSourceProperties {
    public static final String USER = "javax.persistence.jdbc.user";
    public static final String PASSWORD = "javax.persistence.jdbc.password";
    public static final String URL = "javax.persistence.jdbc.url";
    public static final String DRIVER = "javax.persistence.jdbc.driver";
    public static final String PROVIDER = "javax.persistence.provider";
    public static final String TX_TYPE = "javax.persistence.transactionType";
    public static final String JPA_UNIT = "westty.jpa.unit";
    public static final String JPA_PROPERTIES_FILE = "jpa.properties";

    public WesttyJpaProperties() {
        DataSourceProperties ds = new DataSourceProperties();
        setPropertyIfNotSet(JPA_UNIT, "westty-jpa-unit");
        setPropertyIfNotSet(USER, ds.getUsername());
        setPropertyIfNotSet(PASSWORD, ds.getPassword());
        setPropertyIfNotSet(URL, ds.getUrl());
        setPropertyIfNotSet(DRIVER, ds.getDriver());
        setPropertyIfNotSet(PROVIDER, "org.hibernate.ejb.HibernatePersistence");
        setPropertyIfNotSet(TX_TYPE, "RESOURCE_LOCAL");
        setPropertyIfNotSet("hibernate.show_sql", "false");
        setPropertyIfNotSet("hibernate.hbm2ddl.auto", "none");
        setPropertyIfNotSet("hibernate.dialect", "org.hibernate.dialect.DerbyTenSevenDialect");
        WesttyProperties
                .loadProperties(new File(WesttyProperties.getConfDir(), JPA_PROPERTIES_FILE));

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
        super.setUsername(username);
    }

    public String getPassword() {
        return getProperty(PASSWORD);
    }

    public void setPassword(String password) {
        setProperty(PASSWORD, password);
        super.setPassword(password);
    }

    public String getUrl() {
        return getProperty(URL);
    }

    public void setUrl(String url) {
        setProperty(URL, url);
        super.setUrl(url);
    }

    public String getDriver() {
        return getProperty(DRIVER);
    }

    public void setDriver(String driver) {
        setProperty(DRIVER, driver);
        super.setDriver(driver);
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
}
