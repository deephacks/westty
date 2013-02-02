package org.deephacks.westty.internal.job;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.quartz.utils.ConnectionProvider;

public class JobConnectionProvider implements ConnectionProvider {
	private DataSource dataSource;

	public JobConnectionProvider(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	public String getDataSourceName() {
		return dataSource.getClass().getName();
	}

	@Override
	public void shutdown() throws SQLException {
	}

}
