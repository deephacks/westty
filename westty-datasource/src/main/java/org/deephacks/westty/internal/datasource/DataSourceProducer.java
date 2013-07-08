package org.deephacks.westty.internal.datasource;

import com.jolbox.bonecp.BoneCPDataSource;
import org.deephacks.westty.datasource.DataSourceProperties;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

class DataSourceProducer {

    @Singleton
    @Produces
    public static javax.sql.DataSource produceDataSource() {
        DataSourceProperties prop = new DataSourceProperties();
        try {
            Class.forName(prop.getDriver());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        BoneCPDataSource ds = new BoneCPDataSource();
        ds.setUser(prop.getUsername());
        ds.setPassword(prop.getPassword());
        ds.setJdbcUrl(prop.getUrl());
        DataSource datasource = new DataSource(ds);
        return datasource;
    }
}
