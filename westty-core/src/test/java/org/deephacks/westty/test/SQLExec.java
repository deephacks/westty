package org.deephacks.westty.test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import javax.inject.Singleton;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Singleton
public class SQLExec {
    private DataSource datasource;

    private String username;
    private String password;
    private String url;

    public SQLExec(){

    }

    public SQLExec(String username, String password, String url) {
        this.username = username;
        this.password = password;
        this.url = url;
    }

    public void executeResource(String ddl, boolean ignoreSqlEx) throws SQLException, IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        URL ddlUrl = cl.getResource(ddl);
        List<String> lines = Resources.readLines(ddlUrl, Charsets.UTF_8);
        execute(lines, ignoreSqlEx);
    }

    public void execute(File file, boolean ignoreSqlEx) throws SQLException, IOException {
        execute(file, ignoreSqlEx);
    }

    public void execute(List<String> commands, boolean ignoreSqlEx) throws SQLException,
            IOException {
        Connection c = getConnection();
        execute(commands, c, ignoreSqlEx);
    }

    public void execute(File file, String url, String username, String password, boolean ignoreSqlEx)
            throws SQLException, IOException {
        Connection c = getConnection();
        execute(file, c, ignoreSqlEx);
    }

    public Connection getConnection() throws SQLException {
        Connection c = null;
        if (datasource != null) {
            c = datasource.getConnection();
        } else {
            Properties connectionProps = new Properties();
            connectionProps.put("user", username);
            connectionProps.put("password", password);
            c = DriverManager.getConnection(url, connectionProps);
        }
        c.setAutoCommit(true);
        return c;
    }

    private void execute(File f, Connection c, boolean ignoreSqlEx) throws SQLException,
            IOException {
        execute(Files.readLines(f, Charset.defaultCharset()), c, ignoreSqlEx);
    }
    public void execute(String... lines) throws SQLException, IOException {
        execute(Arrays.asList(lines), getConnection(), false);
    }
    private void execute(List<String> lines, Connection c, boolean ignoreSqlEx)
            throws SQLException, IOException {
        try {
            List<String> sqlStmts = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            for (String input : lines) {
                if (input == null || "".equals(input.trim()) || input.startsWith("--")
                        || input.startsWith("#")) {
                    continue;
                }
                sb.append(input);
                if (input.endsWith(";")) {
                    sqlStmts.add(sb.substring(0, sb.length() - 1));
                    sb = new StringBuilder();
                }
            }
            for (String sql : sqlStmts) {
                PreparedStatement stmt = c.prepareStatement(sql);
                stmt.execute();
            }

        } catch (SQLException e) {
            if (!ignoreSqlEx) {
                throw e;
            }
        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
