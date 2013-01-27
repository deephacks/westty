package org.deephacks.westty.datasource;

import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.deephacks.westty.spi.WesttyModule;

import com.google.common.base.Strings;
import com.jolbox.bonecp.BoneCPDataSource;

public class WesttyDataSourceModule implements WesttyModule {
    public static final int LOAD_ORDER = 100;
    public static final String DS_PASSWORD = "westty.ds.password";
    public static final String DS_USERNAME = "westty.ds.username";
    public static final String DS_DRIVER = "westty.ds.driver";
    public static final String DS_URL = "westty.ds.url";

    private static WesttyDataSource DATASOURCE;
    private static Properties PROPS;
    private static String userName;
    private static String password;
    private static String url;
    private static String driver;

    @Override
    public void startup(Properties props) {
        WesttyDataSourceModule.PROPS = props;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public int getLoadOrder() {
        return LOAD_ORDER;
    }

    public static String getUsername() {
        return userName;
    }

    public static String getPassword() {
        return password;
    }

    public static String getUrl() {
        return url;
    }

    public static String getDriver() {
        return driver;
    }

    @Produces
    @ApplicationScoped
    @DataSource
    public WesttyDataSource getDataSource() {
        return get();
    }

    public static WesttyDataSource get() {
        if (DATASOURCE != null) {
            return DATASOURCE;
        }
        String username = getProperty(DS_USERNAME, PROPS);
        String password = getProperty(DS_PASSWORD, PROPS);
        String driver = getProperty(DS_DRIVER, PROPS);
        String url = getProperty(DS_URL, PROPS);
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        BoneCPDataSource ds = new BoneCPDataSource();
        ds.setUser(username);
        ds.setPassword(password);
        ds.setJdbcUrl(url);
        DATASOURCE = new WesttyDataSource(ds);

        return DATASOURCE;
    }

    public static String getProperty(String prop, Properties props) {
        String value = props.getProperty(prop);
        if (Strings.isNullOrEmpty(value)) {
            throw new IllegalArgumentException(DS_USERNAME + " property missing.");
        }
        return value;
    }

}
