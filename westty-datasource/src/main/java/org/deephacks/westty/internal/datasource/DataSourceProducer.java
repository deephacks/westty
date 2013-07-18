package org.deephacks.westty.internal.datasource;

import com.jolbox.bonecp.BoneCPDataSource;
import org.deephacks.westty.config.DataSourceConfig;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

class DataSourceProducer {
    @Inject
    private DataSourceConfig config;

    @Singleton
    @Produces
    public javax.sql.DataSource produceDataSource() {
        try {
            Class.forName(config.getDriver());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        BoneCPDataSource ds = new BoneCPDataSource();
        ds.setUser(config.getUser());
        ds.setPassword(config.getPassword());
        ds.setJdbcUrl(config.getUrl());
        DataSource datasource = new DataSource(ds);
        return datasource;
    }
}
