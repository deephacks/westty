package org.deephacks.westty.job;

import java.sql.Connection;
import java.sql.SQLException;

import org.quartz.utils.ConnectionProvider;

public class JobConnectionProvider implements ConnectionProvider {

    @Override
    public Connection getConnection() throws SQLException {
        return null;
    }

    @Override
    public void shutdown() throws SQLException {

    }

}
