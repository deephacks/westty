package org.deephacks.westty.internal.datasource;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import javax.sql.DataSource;

import org.deephacks.westty.datasource.DataSourceProperties;

import com.jolbox.bonecp.BoneCPDataSource;

class DataSourceProducer {

    @Singleton
    @Produces
    public static DataSource produceDataSource() {
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
        WesttyDataSource datasource = new WesttyDataSource(ds);
        return datasource;
    }
}
