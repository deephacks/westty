package org.deephacks.westty.config;

import org.deephacks.tools4j.config.Config;

import java.util.Properties;

@Config(name="jpa")
public class JpaConfig {
    public static final String JPA_UNIT = "unit";
    public static final String JPA_UNIT_DEFAULT = "westty.jpa.unit";
    public static final String USER = "javax.persistence.jdbc.user";
    public static final String PASSWORD = "javax.persistence.jdbc.password";
    public static final String URL = "javax.persistence.jdbc.url";
    public static final String DRIVER = "javax.persistence.jdbc.driver";
    public static final String PROVIDER = "javax.persistence.provider";
    public static final String TX_TYPE = "javax.persistence.transactionType";
    public static final String VALIDATION_PRE_PERSIST = "javax.persistence.validation.group.pre-persist";
    public static final String VALIDATION_PRE_UPDATE = "javax.persistence.validation.group.pre-update";
    public static final String VALIDATION_PRE_REMOVE = "javax.persistence.transactionType";
    public static final String HIBERNATE_DIALECT = "hibernate.dialect";
    public static final String HIBERNATE_HBM2DDL_AUTO = "hibernate.hbm2ddl.auto";
    public static final String HIBERNATE_SHOW_SQL = "hibernate.show_sql";


    @Config(name = JPA_UNIT)
    private String unit = JPA_UNIT_DEFAULT;

    @Config(name = USER)
    private String user;

    @Config(name = PASSWORD)
    private String password;

    @Config(name = URL)
    private String url;

    @Config(name = DRIVER)
    private String driver;

    @Config(name = PROVIDER)
    private String provider = "org.hibernate.ejb.HibernatePersistence";

    @Config(name = TX_TYPE)
    private String txType = "RESOURCE_LOCAL";

    @Config(name = VALIDATION_PRE_PERSIST)
    private String validationPrePersist;

    @Config(name = VALIDATION_PRE_UPDATE)
    private String validationPreUpdate;

    @Config(name = VALIDATION_PRE_REMOVE)
    private String validationPreRemove;

    @Config(name = HIBERNATE_SHOW_SQL)
    private Boolean hibernateShowSql = false;

    @Config(name = HIBERNATE_HBM2DDL_AUTO)
    private Boolean hibernateHbm2ddlAuto = false;

    @Config(name = HIBERNATE_DIALECT)
    private String hibernateDialect = "org.hibernate.dialect.DerbyTenSevenDialect";

    @Config
    private DataSourceConfig dataSourceConfig;

    public Properties getProperties() {
        Properties properties = new Properties();
        properties.setProperty(JPA_UNIT_DEFAULT, getJpaUnit());
        properties.setProperty(USER, getUsername());
        properties.setProperty(PASSWORD, getPassword());
        properties.setProperty(URL, getUrl());
        properties.setProperty(DRIVER, getDriver());
        properties.setProperty(PROVIDER, getProvider());
        properties.setProperty(TX_TYPE, getTxType());
        if (getValidationPrePersist() != null) {
            properties.setProperty(VALIDATION_PRE_PERSIST, getValidationPrePersist());
        }
        if (getValidationPreRemove() != null) {
            properties.setProperty(VALIDATION_PRE_REMOVE, getValidationPreRemove());
        }
        if (getValidationPreUpdate() != null) {
            properties.setProperty(VALIDATION_PRE_UPDATE, getValidationPreUpdate());
        }

        properties.setProperty(HIBERNATE_SHOW_SQL, Boolean.toString(getHibernateShowSql()));
        properties.setProperty(HIBERNATE_HBM2DDL_AUTO, Boolean.toString(getHibernateHbm2ddlAuto()));
        properties.setProperty(HIBERNATE_DIALECT, getHibernateDialect());
        return properties;
    }

    public String getJpaUnit() {
        return unit;
    }

    public String getUsername() {
        if (user == null) {
            initDataSourceConfig();
            return dataSourceConfig.getUser();
        }
        return user;
    }

    public String getPassword() {
        if (password == null) {
            initDataSourceConfig();
            return dataSourceConfig.getPassword();
        }
        return password;
    }

    public String getUrl() {
        if (url == null) {
            initDataSourceConfig();
            return dataSourceConfig.getUrl();
        }
        return url;
    }

    public String getDriver() {
        if (driver == null) {
            initDataSourceConfig();
            return dataSourceConfig.getDriver();
        }
        return driver;
    }

    public String getProvider() {
        return provider;
    }

    public String getTxType() {
        return txType;
    }

    public String getValidationPrePersist() {
        return validationPrePersist;
    }

    public String getValidationPreUpdate() {
        return validationPreUpdate;
    }

    public String getValidationPreRemove() {
        return validationPreRemove;
    }

    public boolean getHibernateShowSql() {
        return hibernateShowSql;
    }

    public boolean getHibernateHbm2ddlAuto() {
        return hibernateHbm2ddlAuto;
    }

    public String getHibernateDialect() {
        return hibernateDialect;
    }

    private void initDataSourceConfig() {
        if (dataSourceConfig == null) {
            dataSourceConfig = new DataSourceConfig();
        }
    }
}
