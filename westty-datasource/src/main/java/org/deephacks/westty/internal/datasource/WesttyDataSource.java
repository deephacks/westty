package org.deephacks.westty.internal.datasource;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

import org.deephacks.westty.datasource.WesttyDataSourceProperties;
import org.deephacks.westty.properties.WesttyProperties;
import org.deephacks.westty.spi.WesttyModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jolbox.bonecp.BoneCPDataSource;

@Singleton
class WesttyDataSource implements WesttyModule, DataSource {
    private final DataSource ds;

    @Inject
    public WesttyDataSource(WesttyProperties properties) {
        WesttyDataSourceProperties prop = new WesttyDataSourceProperties(properties);
        try {
            Class.forName(prop.getDriver());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        BoneCPDataSource ds = new BoneCPDataSource();
        ds.setUser(prop.getUsername());
        ds.setPassword(prop.getPassword());
        ds.setJdbcUrl(prop.getUrl());
        this.ds = ds;
    }

    @Override
    public void startup() {
    }

    @Override
    public void shutdown() {

    }

    @Override
    public int priority() {
        return 1000;
    }

    public PrintWriter getLogWriter() throws SQLException {
        return ds.getLogWriter();
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return ds.unwrap(iface);
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        ds.setLogWriter(out);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return ds.isWrapperFor(iface);
    }

    public Connection getConnection() throws SQLException {
        return new WesttyConnection(ds.getConnection());
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        ds.setLoginTimeout(seconds);
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return new WesttyConnection(ds.getConnection(username, password));
    }

    public int getLoginTimeout() throws SQLException {
        return ds.getLoginTimeout();
    }

    private static final class WesttyConnection implements Connection {
        private Logger log = LoggerFactory.getLogger(WesttyConnection.class);
        private final Connection con;

        public WesttyConnection(Connection con) {
            this.con = con;
        }

        public <T> T unwrap(Class<T> iface) throws SQLException {
            return con.unwrap(iface);
        }

        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return con.isWrapperFor(iface);
        }

        public Statement createStatement() throws SQLException {
            return new WesttyStatement(con.createStatement());
        }

        public PreparedStatement prepareStatement(String sql) throws SQLException {
            return new WesttyPreparedStatement(con.prepareStatement(sql), sql);
        }

        public CallableStatement prepareCall(String sql) throws SQLException {
            return new WesttyCallableStatement(con.prepareCall(sql), sql);
        }

        public String nativeSQL(String sql) throws SQLException {
            log.debug("{}", sql);
            return con.nativeSQL(sql);
        }

        public void setAutoCommit(boolean autoCommit) throws SQLException {
            con.setAutoCommit(autoCommit);
        }

        public boolean getAutoCommit() throws SQLException {
            return con.getAutoCommit();
        }

        public void commit() throws SQLException {
            con.commit();
        }

        public void rollback() throws SQLException {
            con.rollback();
        }

        public void close() throws SQLException {
            con.close();
        }

        public boolean isClosed() throws SQLException {
            return con.isClosed();
        }

        public DatabaseMetaData getMetaData() throws SQLException {
            return con.getMetaData();
        }

        public void setReadOnly(boolean readOnly) throws SQLException {
            con.setReadOnly(readOnly);
        }

        public boolean isReadOnly() throws SQLException {
            return con.isReadOnly();
        }

        public void setCatalog(String catalog) throws SQLException {
            con.setCatalog(catalog);
        }

        public String getCatalog() throws SQLException {
            return con.getCatalog();
        }

        public void setTransactionIsolation(int level) throws SQLException {
            con.setTransactionIsolation(level);
        }

        public int getTransactionIsolation() throws SQLException {
            return con.getTransactionIsolation();
        }

        public SQLWarning getWarnings() throws SQLException {
            return con.getWarnings();
        }

        public void clearWarnings() throws SQLException {
            con.clearWarnings();
        }

        public Statement createStatement(int resultSetType, int resultSetConcurrency)
                throws SQLException {
            return new WesttyStatement(con.createStatement(resultSetType, resultSetConcurrency));
        }

        public PreparedStatement prepareStatement(String sql, int resultSetType,
                int resultSetConcurrency) throws SQLException {
            return new WesttyPreparedStatement(con.prepareStatement(sql, resultSetType,
                    resultSetConcurrency));
        }

        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
                throws SQLException {
            return new WesttyCallableStatement(con.prepareCall(sql, resultSetType,
                    resultSetConcurrency));
        }

        public Map<String, Class<?>> getTypeMap() throws SQLException {
            return con.getTypeMap();
        }

        public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
            con.setTypeMap(map);
        }

        public void setHoldability(int holdability) throws SQLException {
            con.setHoldability(holdability);
        }

        public int getHoldability() throws SQLException {
            return con.getHoldability();
        }

        public Savepoint setSavepoint() throws SQLException {
            return con.setSavepoint();
        }

        public Savepoint setSavepoint(String name) throws SQLException {
            return con.setSavepoint(name);
        }

        public void rollback(Savepoint savepoint) throws SQLException {
            con.rollback(savepoint);
        }

        public void releaseSavepoint(Savepoint savepoint) throws SQLException {
            con.releaseSavepoint(savepoint);
        }

        public Statement createStatement(int resultSetType, int resultSetConcurrency,
                int resultSetHoldability) throws SQLException {
            return new WesttyStatement(con.createStatement(resultSetType, resultSetConcurrency,
                    resultSetHoldability));
        }

        public PreparedStatement prepareStatement(String sql, int resultSetType,
                int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return new WesttyPreparedStatement(con.prepareStatement(sql, resultSetType,
                    resultSetConcurrency, resultSetHoldability));
        }

        public CallableStatement prepareCall(String sql, int resultSetType,
                int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return new WesttyCallableStatement(con.prepareCall(sql, resultSetType,
                    resultSetConcurrency, resultSetHoldability));
        }

        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
                throws SQLException {
            return new WesttyPreparedStatement(con.prepareStatement(sql, autoGeneratedKeys));
        }

        public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
                throws SQLException {
            return new WesttyPreparedStatement(con.prepareStatement(sql, columnIndexes));
        }

        public PreparedStatement prepareStatement(String sql, String[] columnNames)
                throws SQLException {
            return new WesttyPreparedStatement(con.prepareStatement(sql, columnNames));
        }

        public Clob createClob() throws SQLException {
            return con.createClob();
        }

        public Blob createBlob() throws SQLException {
            return con.createBlob();
        }

        public NClob createNClob() throws SQLException {
            return con.createNClob();
        }

        public SQLXML createSQLXML() throws SQLException {
            return con.createSQLXML();
        }

        public boolean isValid(int timeout) throws SQLException {
            return con.isValid(timeout);
        }

        public void setClientInfo(String name, String value) throws SQLClientInfoException {
            con.setClientInfo(name, value);
        }

        public void setClientInfo(Properties properties) throws SQLClientInfoException {
            con.setClientInfo(properties);
        }

        public String getClientInfo(String name) throws SQLException {
            return con.getClientInfo(name);
        }

        public Properties getClientInfo() throws SQLException {
            return con.getClientInfo();
        }

        public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
            return con.createArrayOf(typeName, elements);
        }

        public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
            return con.createStruct(typeName, attributes);
        }

    }

    private static class WesttyPreparedStatement implements PreparedStatement {
        private final Logger log = LoggerFactory.getLogger(WesttyConnection.class);
        protected final HashMap<Object, Object> data = new HashMap<Object, Object>();
        private final PreparedStatement stmt;
        private final String sql;

        private WesttyPreparedStatement(PreparedStatement stmt, String sql) {
            this.stmt = stmt;
            this.sql = sql;
        }

        private WesttyPreparedStatement(PreparedStatement stmt) {
            this(stmt, null);
        }

        public ResultSet executeQuery(String sql) throws SQLException {
            log.debug("executeQuery {} {}", sql, data);
            return stmt.executeQuery(sql);
        }

        public ResultSet executeQuery() throws SQLException {
            log.debug("executeQuery {} {}", sql, data);
            return stmt.executeQuery();
        }

        public int executeUpdate(String sql) throws SQLException {
            log.debug("executeUpdate {} {}", sql, data);
            return stmt.executeUpdate(sql);
        }

        public int executeUpdate() throws SQLException {
            log.debug("executeUpdate {} {}", sql, data);
            return stmt.executeUpdate();
        }

        public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
            log.debug("executeUpdate {} {}", sql, data);
            return stmt.executeUpdate(sql, autoGeneratedKeys);
        }

        public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
            log.debug("executeUpdate {} {}", sql, data);
            return stmt.executeUpdate(sql, columnIndexes);
        }

        public int executeUpdate(String sql, String[] columnNames) throws SQLException {
            log.debug("executeUpdate {} {}", sql, data);
            return stmt.executeUpdate(sql, columnNames);
        }

        public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
            log.debug("execute {} {}", sql, data);
            return stmt.execute(sql, autoGeneratedKeys);
        }

        public boolean execute(String sql, int[] columnIndexes) throws SQLException {
            log.debug("execute {} {}", sql, data);
            return stmt.execute(sql, columnIndexes);
        }

        public boolean execute(String sql, String[] columnNames) throws SQLException {
            log.debug("execute {} {}", sql, data);
            return stmt.execute(sql, columnNames);
        }

        public boolean execute(String sql) throws SQLException {
            log.debug("execute {} {}", sql, data);
            return stmt.execute(sql);
        }

        public boolean execute() throws SQLException {
            log.debug("execute {} {}", sql, data);
            return stmt.execute();
        }

        public void addBatch(String sql) throws SQLException {
            log.debug("addBatch {} {}", sql, data);
            stmt.addBatch(sql);
        }

        public void clearBatch() throws SQLException {
            log.debug("clearBatch {} {}", sql, data);
            stmt.clearBatch();
        }

        public void addBatch() throws SQLException {
            log.debug("addBatch {} {}", sql, data);
            stmt.addBatch();
        }

        public int[] executeBatch() throws SQLException {
            log.debug("executeBatch {} {}", sql, data);
            return stmt.executeBatch();
        }

        public <T> T unwrap(Class<T> iface) throws SQLException {
            return stmt.unwrap(iface);
        }

        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return stmt.isWrapperFor(iface);
        }

        public void setNull(int parameterIndex, int sqlType) throws SQLException {
            stmt.setNull(parameterIndex, sqlType);
        }

        public void close() throws SQLException {
            stmt.close();
        }

        public int getMaxFieldSize() throws SQLException {
            return stmt.getMaxFieldSize();
        }

        public void setBoolean(int parameterIndex, boolean x) throws SQLException {
            data.put(parameterIndex, x);
            stmt.setBoolean(parameterIndex, x);
        }

        public void setByte(int parameterIndex, byte x) throws SQLException {
            data.put(parameterIndex, x);
            stmt.setByte(parameterIndex, x);
        }

        public void setMaxFieldSize(int max) throws SQLException {
            stmt.setMaxFieldSize(max);
        }

        public void setShort(int parameterIndex, short x) throws SQLException {
            data.put(parameterIndex, x);
            stmt.setShort(parameterIndex, x);
        }

        public int getMaxRows() throws SQLException {
            return stmt.getMaxRows();
        }

        public void setInt(int parameterIndex, int x) throws SQLException {
            data.put(parameterIndex, x);
            stmt.setInt(parameterIndex, x);
        }

        public void setMaxRows(int max) throws SQLException {
            stmt.setMaxRows(max);
        }

        public void setLong(int parameterIndex, long x) throws SQLException {
            data.put(parameterIndex, x);
            stmt.setLong(parameterIndex, x);
        }

        public void setEscapeProcessing(boolean enable) throws SQLException {
            stmt.setEscapeProcessing(enable);
        }

        public void setFloat(int parameterIndex, float x) throws SQLException {
            data.put(parameterIndex, x);
            stmt.setFloat(parameterIndex, x);
        }

        public void setDouble(int parameterIndex, double x) throws SQLException {
            data.put(parameterIndex, x);
            stmt.setDouble(parameterIndex, x);
        }

        public int getQueryTimeout() throws SQLException {

            return stmt.getQueryTimeout();
        }

        public void setQueryTimeout(int seconds) throws SQLException {
            stmt.setQueryTimeout(seconds);
        }

        public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
            data.put(parameterIndex, x);
            stmt.setBigDecimal(parameterIndex, x);
        }

        public void setString(int parameterIndex, String x) throws SQLException {
            data.put(parameterIndex, x);
            stmt.setString(parameterIndex, x);
        }

        public void setBytes(int parameterIndex, byte[] x) throws SQLException {
            data.put(parameterIndex, x);
            stmt.setBytes(parameterIndex, x);
        }

        public void cancel() throws SQLException {
            stmt.cancel();
        }

        public SQLWarning getWarnings() throws SQLException {
            return stmt.getWarnings();
        }

        public void setDate(int parameterIndex, Date x) throws SQLException {
            data.put(parameterIndex, x);
            stmt.setDate(parameterIndex, x);
        }

        public void setTime(int parameterIndex, Time x) throws SQLException {
            data.put(parameterIndex, x);
            stmt.setTime(parameterIndex, x);
        }

        public void clearWarnings() throws SQLException {
            stmt.clearWarnings();
        }

        public void setCursorName(String name) throws SQLException {
            stmt.setCursorName(name);
        }

        public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
            data.put(parameterIndex, x);
            stmt.setTimestamp(parameterIndex, x);
        }

        public void setAsciiStream(int parameterIndex, InputStream x, int length)
                throws SQLException {
            stmt.setAsciiStream(parameterIndex, x, length);
        }

        @SuppressWarnings("deprecation")
        public void setUnicodeStream(int parameterIndex, InputStream x, int length)
                throws SQLException {
            stmt.setUnicodeStream(parameterIndex, x, length);
        }

        public ResultSet getResultSet() throws SQLException {
            return stmt.getResultSet();
        }

        public void setBinaryStream(int parameterIndex, InputStream x, int length)
                throws SQLException {
            stmt.setBinaryStream(parameterIndex, x, length);
        }

        public int getUpdateCount() throws SQLException {
            return stmt.getUpdateCount();
        }

        public boolean getMoreResults() throws SQLException {
            return stmt.getMoreResults();
        }

        public void clearParameters() throws SQLException {
            stmt.clearParameters();
        }

        public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
            data.put(parameterIndex, x);
            stmt.setObject(parameterIndex, x, targetSqlType);
        }

        public void setFetchDirection(int direction) throws SQLException {
            stmt.setFetchDirection(direction);
        }

        public int getFetchDirection() throws SQLException {
            return stmt.getFetchDirection();
        }

        public void setObject(int parameterIndex, Object x) throws SQLException {
            data.put(parameterIndex, x);
            stmt.setObject(parameterIndex, x);
        }

        public void setFetchSize(int rows) throws SQLException {
            stmt.setFetchSize(rows);
        }

        public int getFetchSize() throws SQLException {
            return stmt.getFetchSize();
        }

        public int getResultSetConcurrency() throws SQLException {
            return stmt.getResultSetConcurrency();
        }

        public int getResultSetType() throws SQLException {
            return stmt.getResultSetType();
        }

        public void setCharacterStream(int parameterIndex, Reader reader, int length)
                throws SQLException {
            stmt.setCharacterStream(parameterIndex, reader, length);
        }

        public void setRef(int parameterIndex, Ref x) throws SQLException {
            stmt.setRef(parameterIndex, x);
        }

        public void setBlob(int parameterIndex, Blob x) throws SQLException {
            data.put(parameterIndex, x);
            stmt.setBlob(parameterIndex, x);
        }

        public void setClob(int parameterIndex, Clob x) throws SQLException {
            data.put(parameterIndex, x);
            stmt.setClob(parameterIndex, x);
        }

        public Connection getConnection() throws SQLException {
            return stmt.getConnection();
        }

        public void setArray(int parameterIndex, Array x) throws SQLException {
            data.put(parameterIndex, x);
            stmt.setArray(parameterIndex, x);
        }

        public ResultSetMetaData getMetaData() throws SQLException {
            return stmt.getMetaData();
        }

        public boolean getMoreResults(int current) throws SQLException {
            return stmt.getMoreResults(current);
        }

        public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
            data.put(parameterIndex, x);
            stmt.setDate(parameterIndex, x, cal);
        }

        public ResultSet getGeneratedKeys() throws SQLException {
            return stmt.getGeneratedKeys();
        }

        public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
            data.put(parameterIndex, x);
            stmt.setTime(parameterIndex, x, cal);
        }

        public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
            data.put(parameterIndex, x);
            stmt.setTimestamp(parameterIndex, x, cal);
        }

        public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
            data.put(parameterIndex, null);
            stmt.setNull(parameterIndex, sqlType, typeName);
        }

        public void setURL(int parameterIndex, URL x) throws SQLException {
            data.put(parameterIndex, x);
            stmt.setURL(parameterIndex, x);
        }

        public ParameterMetaData getParameterMetaData() throws SQLException {
            return stmt.getParameterMetaData();
        }

        public void setRowId(int parameterIndex, RowId x) throws SQLException {
            data.put(parameterIndex, x);
            stmt.setRowId(parameterIndex, x);
        }

        public void setNString(int parameterIndex, String value) throws SQLException {
            data.put(parameterIndex, value);
            stmt.setNString(parameterIndex, value);
        }

        public void setNCharacterStream(int parameterIndex, Reader value, long length)
                throws SQLException {
            stmt.setNCharacterStream(parameterIndex, value, length);
        }

        public void setNClob(int parameterIndex, NClob value) throws SQLException {
            data.put(parameterIndex, value);
            stmt.setNClob(parameterIndex, value);
        }

        public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
            stmt.setClob(parameterIndex, reader, length);
        }

        public void setBlob(int parameterIndex, InputStream inputStream, long length)
                throws SQLException {
            stmt.setBlob(parameterIndex, inputStream, length);
        }

        public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
            stmt.setNClob(parameterIndex, reader, length);
        }

        public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
            stmt.setSQLXML(parameterIndex, xmlObject);
        }

        public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength)
                throws SQLException {
            data.put(parameterIndex, x);
            stmt.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
        }

        public int getResultSetHoldability() throws SQLException {
            return stmt.getResultSetHoldability();
        }

        public boolean isClosed() throws SQLException {
            return stmt.isClosed();
        }

        public void setPoolable(boolean poolable) throws SQLException {
            stmt.setPoolable(poolable);
        }

        public boolean isPoolable() throws SQLException {
            return stmt.isPoolable();
        }

        public void setAsciiStream(int parameterIndex, InputStream x, long length)
                throws SQLException {
            stmt.setAsciiStream(parameterIndex, x, length);
        }

        public void setBinaryStream(int parameterIndex, InputStream x, long length)
                throws SQLException {
            stmt.setBinaryStream(parameterIndex, x, length);
        }

        public void setCharacterStream(int parameterIndex, Reader reader, long length)
                throws SQLException {
            stmt.setCharacterStream(parameterIndex, reader, length);
        }

        public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
            stmt.setAsciiStream(parameterIndex, x);
        }

        public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
            stmt.setBinaryStream(parameterIndex, x);
        }

        public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
            stmt.setCharacterStream(parameterIndex, reader);
        }

        public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
            stmt.setNCharacterStream(parameterIndex, value);
        }

        public void setClob(int parameterIndex, Reader reader) throws SQLException {
            stmt.setClob(parameterIndex, reader);
        }

        public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
            stmt.setBlob(parameterIndex, inputStream);
        }

        public void setNClob(int parameterIndex, Reader reader) throws SQLException {
            stmt.setNClob(parameterIndex, reader);
        }
    }

    private static final class WesttyStatement implements Statement {
        private static final Logger log = LoggerFactory.getLogger(WesttyStatement.class);
        private Statement stmt;

        public WesttyStatement(Statement stmt) {
            this.stmt = stmt;
        }

        public int executeUpdate(String sql) throws SQLException {
            log.info("executeUpdate {}", sql);
            return stmt.executeUpdate(sql);
        }

        public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
            log.info("executeUpdate {}", sql);
            return stmt.executeUpdate(sql, autoGeneratedKeys);
        }

        public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
            log.info("executeUpdate {}", sql);
            return stmt.executeUpdate(sql, columnIndexes);
        }

        public int executeUpdate(String sql, String[] columnNames) throws SQLException {
            log.info("executeUpdate {}", sql);
            return stmt.executeUpdate(sql, columnNames);
        }

        public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
            log.info("execute {}", sql);
            return stmt.execute(sql, autoGeneratedKeys);
        }

        public boolean execute(String sql, int[] columnIndexes) throws SQLException {
            log.info("execute {}", sql);
            return stmt.execute(sql, columnIndexes);
        }

        public boolean execute(String sql, String[] columnNames) throws SQLException {
            log.info("execute {}", sql);
            return stmt.execute(sql, columnNames);
        }

        public ResultSet executeQuery(String sql) throws SQLException {
            log.debug("executeQuery {}", sql);
            return stmt.executeQuery(sql);
        }

        public boolean execute(String sql) throws SQLException {
            log.debug("execute {}", sql);
            return stmt.execute(sql);
        }

        public void addBatch(String sql) throws SQLException {
            log.debug("addBatch {}", sql);
            stmt.addBatch(sql);
        }

        public void clearBatch() throws SQLException {
            log.debug("clearBatch");
            stmt.clearBatch();
        }

        public int[] executeBatch() throws SQLException {
            log.debug("executeBatch");
            return stmt.executeBatch();
        }

        public <T> T unwrap(Class<T> iface) throws SQLException {
            return stmt.unwrap(iface);
        }

        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return stmt.isWrapperFor(iface);
        }

        public void close() throws SQLException {
            stmt.close();
        }

        public int getMaxFieldSize() throws SQLException {
            return stmt.getMaxFieldSize();
        }

        public void setMaxFieldSize(int max) throws SQLException {
            stmt.setMaxFieldSize(max);
        }

        public int getMaxRows() throws SQLException {
            return stmt.getMaxRows();
        }

        public void setMaxRows(int max) throws SQLException {
            stmt.setMaxRows(max);
        }

        public void setEscapeProcessing(boolean enable) throws SQLException {
            stmt.setEscapeProcessing(enable);
        }

        public int getQueryTimeout() throws SQLException {
            return stmt.getQueryTimeout();
        }

        public void setQueryTimeout(int seconds) throws SQLException {
            stmt.setQueryTimeout(seconds);
        }

        public void cancel() throws SQLException {
            stmt.cancel();
        }

        public SQLWarning getWarnings() throws SQLException {
            return stmt.getWarnings();
        }

        public void clearWarnings() throws SQLException {
            stmt.clearWarnings();
        }

        public void setCursorName(String name) throws SQLException {
            stmt.setCursorName(name);
        }

        public ResultSet getResultSet() throws SQLException {
            return stmt.getResultSet();
        }

        public int getUpdateCount() throws SQLException {
            return stmt.getUpdateCount();
        }

        public boolean getMoreResults() throws SQLException {
            return stmt.getMoreResults();
        }

        public void setFetchDirection(int direction) throws SQLException {
            stmt.setFetchDirection(direction);
        }

        public int getFetchDirection() throws SQLException {
            return stmt.getFetchDirection();
        }

        public void setFetchSize(int rows) throws SQLException {
            stmt.setFetchSize(rows);
        }

        public int getFetchSize() throws SQLException {
            return stmt.getFetchSize();
        }

        public int getResultSetConcurrency() throws SQLException {
            return stmt.getResultSetConcurrency();
        }

        public int getResultSetType() throws SQLException {
            return stmt.getResultSetType();
        }

        public Connection getConnection() throws SQLException {
            return stmt.getConnection();
        }

        public boolean getMoreResults(int current) throws SQLException {
            return stmt.getMoreResults(current);
        }

        public ResultSet getGeneratedKeys() throws SQLException {
            return stmt.getGeneratedKeys();
        }

        public int getResultSetHoldability() throws SQLException {
            return stmt.getResultSetHoldability();
        }

        public boolean isClosed() throws SQLException {
            return stmt.isClosed();
        }

        public void setPoolable(boolean poolable) throws SQLException {
            stmt.setPoolable(poolable);
        }

        public boolean isPoolable() throws SQLException {
            return stmt.isPoolable();
        }
    }

    private static final class WesttyCallableStatement extends WesttyPreparedStatement implements
            CallableStatement {

        private CallableStatement stmt;

        private WesttyCallableStatement(CallableStatement stmt) {
            super(stmt);
        }

        private WesttyCallableStatement(CallableStatement stmt, String sql) {
            super(stmt, sql);
        }

        public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException {
            stmt.registerOutParameter(parameterIndex, sqlType);
        }

        public void registerOutParameter(int parameterIndex, int sqlType, int scale)
                throws SQLException {
            stmt.registerOutParameter(parameterIndex, sqlType, scale);
        }

        public boolean wasNull() throws SQLException {
            return stmt.wasNull();
        }

        public String getString(int parameterIndex) throws SQLException {
            return stmt.getString(parameterIndex);
        }

        public boolean getBoolean(int parameterIndex) throws SQLException {
            return stmt.getBoolean(parameterIndex);
        }

        public byte getByte(int parameterIndex) throws SQLException {
            return stmt.getByte(parameterIndex);
        }

        public short getShort(int parameterIndex) throws SQLException {
            return stmt.getShort(parameterIndex);
        }

        public int getInt(int parameterIndex) throws SQLException {
            return stmt.getInt(parameterIndex);
        }

        public long getLong(int parameterIndex) throws SQLException {
            return stmt.getLong(parameterIndex);
        }

        public float getFloat(int parameterIndex) throws SQLException {
            return stmt.getFloat(parameterIndex);
        }

        public double getDouble(int parameterIndex) throws SQLException {
            return stmt.getDouble(parameterIndex);
        }

        @SuppressWarnings("deprecation")
        public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
            return stmt.getBigDecimal(parameterIndex, scale);
        }

        public byte[] getBytes(int parameterIndex) throws SQLException {
            return stmt.getBytes(parameterIndex);
        }

        public Date getDate(int parameterIndex) throws SQLException {
            return stmt.getDate(parameterIndex);
        }

        public Time getTime(int parameterIndex) throws SQLException {
            return stmt.getTime(parameterIndex);
        }

        public Timestamp getTimestamp(int parameterIndex) throws SQLException {
            return stmt.getTimestamp(parameterIndex);
        }

        public Object getObject(int parameterIndex) throws SQLException {
            return stmt.getObject(parameterIndex);
        }

        public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
            return stmt.getBigDecimal(parameterIndex);
        }

        public Object getObject(int parameterIndex, Map<String, Class<?>> map) throws SQLException {
            return stmt.getObject(parameterIndex, map);
        }

        public Ref getRef(int parameterIndex) throws SQLException {
            return stmt.getRef(parameterIndex);
        }

        public Blob getBlob(int parameterIndex) throws SQLException {
            return stmt.getBlob(parameterIndex);
        }

        public Clob getClob(int parameterIndex) throws SQLException {
            return stmt.getClob(parameterIndex);
        }

        public Array getArray(int parameterIndex) throws SQLException {
            return stmt.getArray(parameterIndex);
        }

        public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
            return stmt.getDate(parameterIndex, cal);
        }

        public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
            return stmt.getTime(parameterIndex, cal);
        }

        public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException {
            return stmt.getTimestamp(parameterIndex, cal);
        }

        public Connection getConnection() throws SQLException {
            return stmt.getConnection();
        }

        public void registerOutParameter(String parameterName, int sqlType) throws SQLException {
            stmt.registerOutParameter(parameterName, sqlType);
        }

        public void registerOutParameter(String parameterName, int sqlType, int scale)
                throws SQLException {
            stmt.registerOutParameter(parameterName, sqlType, scale);
        }

        public void registerOutParameter(String parameterName, int sqlType, String typeName)
                throws SQLException {

            stmt.registerOutParameter(parameterName, sqlType, typeName);
        }

        public URL getURL(int parameterIndex) throws SQLException {
            return stmt.getURL(parameterIndex);
        }

        public void setURL(String parameterName, URL val) throws SQLException {
            data.put(parameterName, val);
            stmt.setURL(parameterName, val);
        }

        public void setNull(String parameterName, int sqlType) throws SQLException {
            data.put(parameterName, null);
            stmt.setNull(parameterName, sqlType);
        }

        public void setBoolean(String parameterName, boolean x) throws SQLException {
            data.put(parameterName, x);
            stmt.setBoolean(parameterName, x);
        }

        public void setByte(String parameterName, byte x) throws SQLException {
            data.put(parameterName, x);
            stmt.setByte(parameterName, x);
        }

        public void setShort(String parameterName, short x) throws SQLException {
            data.put(parameterName, x);
            stmt.setShort(parameterName, x);
        }

        public void setInt(String parameterName, int x) throws SQLException {
            data.put(parameterName, x);
            stmt.setInt(parameterName, x);
        }

        public void setLong(String parameterName, long x) throws SQLException {
            data.put(parameterName, x);
            stmt.setLong(parameterName, x);
        }

        public void setFloat(String parameterName, float x) throws SQLException {
            data.put(parameterName, x);
            stmt.setFloat(parameterName, x);
        }

        public void setDouble(String parameterName, double x) throws SQLException {
            data.put(parameterName, x);
            stmt.setDouble(parameterName, x);
        }

        public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
            data.put(parameterName, x);
            stmt.setBigDecimal(parameterName, x);
        }

        public void setString(String parameterName, String x) throws SQLException {
            data.put(parameterName, x);
            stmt.setString(parameterName, x);
        }

        public void setBytes(String parameterName, byte[] x) throws SQLException {
            data.put(parameterName, x);
            stmt.setBytes(parameterName, x);
        }

        public void setDate(String parameterName, Date x) throws SQLException {
            data.put(parameterName, x);
            stmt.setDate(parameterName, x);
        }

        public void setTime(String parameterName, Time x) throws SQLException {
            data.put(parameterName, x);
            stmt.setTime(parameterName, x);
        }

        public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
            data.put(parameterName, x);
            stmt.setTimestamp(parameterName, x);
        }

        public void setAsciiStream(String parameterName, InputStream x, int length)
                throws SQLException {
            data.put(parameterName, x);
            stmt.setAsciiStream(parameterName, x, length);
        }

        public void setBinaryStream(String parameterName, InputStream x, int length)
                throws SQLException {
            stmt.setBinaryStream(parameterName, x, length);
        }

        public void setObject(String parameterName, Object x, int targetSqlType, int scale)
                throws SQLException {
            stmt.setObject(parameterName, x, targetSqlType, scale);
        }

        public void setObject(String parameterName, Object x, int targetSqlType)
                throws SQLException {
            data.put(parameterName, x);
            stmt.setObject(parameterName, x, targetSqlType);
        }

        public void setObject(String parameterName, Object x) throws SQLException {
            data.put(parameterName, x);
            stmt.setObject(parameterName, x);
        }

        public void setCharacterStream(String parameterName, Reader reader, int length)
                throws SQLException {
            stmt.setCharacterStream(parameterName, reader, length);
        }

        public void setDate(String parameterName, Date x, Calendar cal) throws SQLException {
            data.put(parameterName, x);
            stmt.setDate(parameterName, x, cal);
        }

        public void setTime(String parameterName, Time x, Calendar cal) throws SQLException {
            data.put(parameterName, x);
            stmt.setTime(parameterName, x, cal);
        }

        public void setTimestamp(String parameterName, Timestamp x, Calendar cal)
                throws SQLException {
            data.put(parameterName, x);
            stmt.setTimestamp(parameterName, x, cal);
        }

        public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {
            data.put(parameterName, null);
            stmt.setNull(parameterName, sqlType, typeName);
        }

        public String getString(String parameterName) throws SQLException {
            return stmt.getString(parameterName);
        }

        public boolean getBoolean(String parameterName) throws SQLException {
            return stmt.getBoolean(parameterName);
        }

        public byte getByte(String parameterName) throws SQLException {
            return stmt.getByte(parameterName);
        }

        public short getShort(String parameterName) throws SQLException {
            return stmt.getShort(parameterName);
        }

        public int getInt(String parameterName) throws SQLException {
            return stmt.getInt(parameterName);
        }

        public long getLong(String parameterName) throws SQLException {
            return stmt.getLong(parameterName);
        }

        public float getFloat(String parameterName) throws SQLException {
            return stmt.getFloat(parameterName);
        }

        public double getDouble(String parameterName) throws SQLException {
            return stmt.getDouble(parameterName);
        }

        public byte[] getBytes(String parameterName) throws SQLException {
            return stmt.getBytes(parameterName);
        }

        public Date getDate(String parameterName) throws SQLException {
            return stmt.getDate(parameterName);
        }

        public Time getTime(String parameterName) throws SQLException {
            return stmt.getTime(parameterName);
        }

        public Timestamp getTimestamp(String parameterName) throws SQLException {
            return stmt.getTimestamp(parameterName);
        }

        public Object getObject(String parameterName) throws SQLException {
            return stmt.getObject(parameterName);
        }

        public BigDecimal getBigDecimal(String parameterName) throws SQLException {
            return stmt.getBigDecimal(parameterName);
        }

        public Object getObject(String parameterName, Map<String, Class<?>> map)
                throws SQLException {
            return stmt.getObject(parameterName, map);
        }

        public Ref getRef(String parameterName) throws SQLException {
            return stmt.getRef(parameterName);
        }

        public Blob getBlob(String parameterName) throws SQLException {
            return stmt.getBlob(parameterName);
        }

        public Clob getClob(String parameterName) throws SQLException {
            return stmt.getClob(parameterName);
        }

        public Array getArray(String parameterName) throws SQLException {
            return stmt.getArray(parameterName);
        }

        public Date getDate(String parameterName, Calendar cal) throws SQLException {
            return stmt.getDate(parameterName, cal);
        }

        public Time getTime(String parameterName, Calendar cal) throws SQLException {
            return stmt.getTime(parameterName, cal);
        }

        public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException {
            return stmt.getTimestamp(parameterName, cal);
        }

        public URL getURL(String parameterName) throws SQLException {
            return stmt.getURL(parameterName);
        }

        public RowId getRowId(int parameterIndex) throws SQLException {
            return stmt.getRowId(parameterIndex);
        }

        public RowId getRowId(String parameterName) throws SQLException {
            return stmt.getRowId(parameterName);
        }

        public void setRowId(String parameterName, RowId x) throws SQLException {
            data.put(parameterName, x);
            stmt.setRowId(parameterName, x);
        }

        public void setNString(String parameterName, String value) throws SQLException {
            data.put(parameterName, value);
            stmt.setNString(parameterName, value);
        }

        public void setNCharacterStream(String parameterName, Reader value, long length)
                throws SQLException {
            stmt.setNCharacterStream(parameterName, value, length);
        }

        public void setNClob(String parameterName, NClob value) throws SQLException {
            stmt.setNClob(parameterName, value);
        }

        public void setClob(String parameterName, Reader reader, long length) throws SQLException {
            stmt.setClob(parameterName, reader, length);
        }

        public void setBlob(String parameterName, InputStream inputStream, long length)
                throws SQLException {
            stmt.setBlob(parameterName, inputStream, length);
        }

        public void setNClob(String parameterName, Reader reader, long length) throws SQLException {
            stmt.setNClob(parameterName, reader, length);
        }

        public NClob getNClob(int parameterIndex) throws SQLException {
            return stmt.getNClob(parameterIndex);
        }

        public NClob getNClob(String parameterName) throws SQLException {
            return stmt.getNClob(parameterName);
        }

        public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {
            data.put(parameterName, xmlObject);
            stmt.setSQLXML(parameterName, xmlObject);
        }

        public SQLXML getSQLXML(int parameterIndex) throws SQLException {
            return stmt.getSQLXML(parameterIndex);
        }

        public SQLXML getSQLXML(String parameterName) throws SQLException {
            return stmt.getSQLXML(parameterName);
        }

        public String getNString(int parameterIndex) throws SQLException {
            return stmt.getNString(parameterIndex);
        }

        public String getNString(String parameterName) throws SQLException {
            return stmt.getNString(parameterName);
        }

        public Reader getNCharacterStream(int parameterIndex) throws SQLException {
            return stmt.getNCharacterStream(parameterIndex);
        }

        public Reader getNCharacterStream(String parameterName) throws SQLException {
            return stmt.getNCharacterStream(parameterName);
        }

        public Reader getCharacterStream(int parameterIndex) throws SQLException {
            return stmt.getCharacterStream(parameterIndex);
        }

        public Reader getCharacterStream(String parameterName) throws SQLException {
            return stmt.getCharacterStream(parameterName);
        }

        public void setBlob(String parameterName, Blob x) throws SQLException {
            data.put(parameterName, x);
            stmt.setBlob(parameterName, x);
        }

        public void setClob(String parameterName, Clob x) throws SQLException {
            data.put(parameterName, x);
            stmt.setClob(parameterName, x);
        }

        public void setAsciiStream(String parameterName, InputStream x, long length)
                throws SQLException {
            stmt.setAsciiStream(parameterName, x, length);
        }

        public void setBinaryStream(String parameterName, InputStream x, long length)
                throws SQLException {
            stmt.setBinaryStream(parameterName, x, length);
        }

        public void setCharacterStream(String parameterName, Reader reader, long length)
                throws SQLException {
            stmt.setCharacterStream(parameterName, reader, length);
        }

        public void setAsciiStream(String parameterName, InputStream x) throws SQLException {
            stmt.setAsciiStream(parameterName, x);
        }

        public void setBinaryStream(String parameterName, InputStream x) throws SQLException {
            stmt.setBinaryStream(parameterName, x);
        }

        public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
            stmt.setCharacterStream(parameterName, reader);
        }

        public void setNCharacterStream(String parameterName, Reader value) throws SQLException {
            stmt.setNCharacterStream(parameterName, value);
        }

        public void setClob(String parameterName, Reader reader) throws SQLException {
            stmt.setClob(parameterName, reader);
        }

        public void setBlob(String parameterName, InputStream inputStream) throws SQLException {
            stmt.setBlob(parameterName, inputStream);
        }

        public void setNClob(String parameterName, Reader reader) throws SQLException {
            stmt.setNClob(parameterName, reader);
        }

        @Override
        public void registerOutParameter(int parameterIndex, int sqlType, String typeName)
                throws SQLException {

        }

    }
}
