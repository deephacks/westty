package org.deephacks.westty.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.deephacks.westty.jpa.WesttyPersistenceUnitInfo;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;

public class DdlExec {

    public static void executeResource(String ddl, String properties, boolean ignoreSqlEx)
            throws SQLException, IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        URL ddlUrl = cl.getResource(ddl);
        URL propUrl = cl.getResource(properties);

        Properties p = new Properties();
        try {
            p.load(propUrl.openStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String username = p.getProperty(WesttyPersistenceUnitInfo.USER);
        String password = p.getProperty(WesttyPersistenceUnitInfo.PASSWORD);
        String url = p.getProperty(WesttyPersistenceUnitInfo.URL);
        List<String> lines = Resources.readLines(ddlUrl, Charsets.UTF_8);

        execute(lines, url, username, password, ignoreSqlEx);
    }

    public static void execute(File file, File properties, boolean ignoreSqlEx)
            throws SQLException, IOException {
        Properties p = new Properties();
        try {
            p.load(new FileInputStream(properties));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String username = p.getProperty(WesttyPersistenceUnitInfo.USER);
        String password = p.getProperty(WesttyPersistenceUnitInfo.PASSWORD);
        String url = p.getProperty(WesttyPersistenceUnitInfo.URL);
        execute(file, url, username, password, ignoreSqlEx);
    }

    public static void execute(List<String> commands, String url, String username, String password,
            boolean ignoreSqlEx) throws SQLException, IOException {
        Connection c = getConnection(url, username, password);
        execute(commands, c, ignoreSqlEx);
    }

    public static void execute(File file, String url, String username, String password,
            boolean ignoreSqlEx) throws SQLException, IOException {
        Connection c = getConnection(url, username, password);
        execute(file, c, ignoreSqlEx);
    }

    private static Connection getConnection(String url, String username, String password)
            throws SQLException {
        Properties connectionProps = new Properties();
        connectionProps.put("user", username);
        connectionProps.put("password", password);
        Connection conn = DriverManager.getConnection(url, connectionProps);
        conn.setAutoCommit(true);
        return conn;
    }

    private static void execute(File f, Connection c, boolean ignoreSqlEx) throws SQLException,
            IOException {
        try {
            try {
                for (String sql : Files.readLines(f, Charset.defaultCharset())) {
                    if (sql == null || "".equals(sql.trim()) || sql.startsWith("--")) {
                        continue;
                    }
                    PreparedStatement stmt = c.prepareStatement(sql);
                    stmt.execute();
                }
            } catch (SQLException e) {
                if (!ignoreSqlEx) {
                    throw e;
                }
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

    private static void execute(List<String> commands, Connection c, boolean ignoreSqlEx)
            throws SQLException, IOException {
        try {
            try {
                for (String sql : commands) {
                    if (sql == null || "".equals(sql.trim()) || sql.startsWith("--")) {
                        continue;
                    }
                    PreparedStatement stmt = c.prepareStatement(sql);
                    stmt.execute();
                }
            } catch (SQLException e) {
                if (!ignoreSqlEx) {
                    throw e;
                }
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
