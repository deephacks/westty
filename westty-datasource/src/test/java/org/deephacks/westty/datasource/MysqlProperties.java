package org.deephacks.westty.datasource;

import java.util.Properties;

import org.deephacks.westty.properties.WesttyProperties;

public class MysqlProperties extends Properties {
    private static final long serialVersionUID = -2155409639047609415L;

    public MysqlProperties() {
    }

    public MysqlProperties(WesttyProperties properties) {

        WesttyDataSourceProperties ds = new WesttyDataSourceProperties();
        ds.setUsername("westty");
        ds.setPassword("westty");
        ds.setUrl("jdbc:derby:memory:westty;create=true");
        ds.setDriver("org.apache.derby.jdbc.EmbeddedDriver");
        properties.add(ds);

        Properties source = properties.getProperties();
        for (String key : source.stringPropertyNames()) {
            setProperty(key, source.getProperty(key));
        }
    }

}
