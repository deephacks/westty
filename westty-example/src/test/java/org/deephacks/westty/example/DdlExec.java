package org.deephacks.westty.example;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;
import javax.sql.DataSource;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;

public class DdlExec {
    @Inject
    private DataSource datasource;

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
        Connection c = datasource.getConnection();
        execute(commands, c, ignoreSqlEx);
    }

    public void execute(File file, String url, String username, String password, boolean ignoreSqlEx)
            throws SQLException, IOException {
        Connection c = datasource.getConnection();
        execute(file, c, ignoreSqlEx);
    }

    private void execute(File f, Connection c, boolean ignoreSqlEx) throws SQLException,
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

    private void execute(List<String> commands, Connection c, boolean ignoreSqlEx)
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
