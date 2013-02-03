package org.deephacks.westty.jpa;

import java.io.File;

import javax.enterprise.inject.Alternative;

import org.deephacks.westty.datasource.WesttyDataSourceProperties;
import org.deephacks.westty.properties.WesttyProperties;
import org.deephacks.westty.properties.WesttyPropertyBuilder;

@Alternative
public class WesttyJpaProperties extends WesttyDataSourceProperties {
    private static final long serialVersionUID = 1667859287794861790L;
    public static final String USER = "javax.persistence.jdbc.user";
    public static final String PASSWORD = "javax.persistence.jdbc.password";
    public static final String URL = "javax.persistence.jdbc.url";
    public static final String DRIVER = "javax.persistence.jdbc.driver";
    public static final String PROVIDER = "javax.persistence.provider";
    public static final String TX_TYPE = "javax.persistence.transactionType";
    public static final String JPA_UNIT = "westty.jpa.unit";
    public static final String JPA_PROPERTIES_FILE = "jpa.properties";

    public WesttyJpaProperties(WesttyProperties properties) {
        super(properties);
    }

    @WesttyPropertyBuilder(priority = 1000)
    public static void build(WesttyProperties properties) {
        WesttyDataSourceProperties ds = new WesttyDataSourceProperties(properties);
        WesttyJpaProperties jpa = new WesttyJpaProperties(properties);
        jpa.setJpaUnit("westty-jpa-unit");
        jpa.setUsername(ds.getUsername());
        jpa.setPassword(ds.getPassword());
        jpa.setUrl("jdbc:derby:memory:westty");
        jpa.setDriver(ds.getDriver());
        jpa.setProvider("org.hibernate.ejb.HibernatePersistence");
        jpa.setTxType("RESOURCE_LOCAL");
        // jpa.setProperty("hibernate.hbm2ddl.auto", "validate");
        jpa.setProperty("hibernate.show_sql", "false");
        jpa.setProperty("hibernate.dialect", "org.hibernate.dialect.DerbyDialect");
        properties.loadProperties(new File(properties.getConfDir(), JPA_PROPERTIES_FILE));
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
