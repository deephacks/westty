/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.deephacks.westty.config;

import static com.google.common.io.Files.readLines;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import javax.validation.constraints.NotNull;

import org.deephacks.tools4j.config.Config;
import org.deephacks.tools4j.config.ConfigScope;
import org.deephacks.tools4j.config.Id;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;

@Config(desc = JpaConfig.DESC)
@ConfigScope
public class JpaConfig {

    static final String DESC = "Jpa configuration. Changes requires restart.";

    private static final String INSTALL_DDL = "META-INF/install.ddl";
    private static final String UNINSTALL_DDL = "META-INF/uninstall.ddl";
    private static final String USER = "javax.persistence.jdbc.user";
    private static final String PASSWORD = "javax.persistence.jdbc.password";
    private static final String URL = "javax.persistence.jdbc.url";
    private static final String DRIVER = "javax.persistence.jdbc.driver";
    private static final String PROVIDER = "javax.persistence.provider";
    private static final String TX_TYPE = "javax.persistence.transactionType";
    private static final String HIB_DDL = "hibernate.hbm2ddl.auto";
    private static final String HIB_SHOW_SQL = "hibernate.show_sql";
    private static final String HIB_DIALECT = "hibernate.dialect";

    @Id(desc = JpaConfig.DESC)
    public static final String ID = "westty.jpa";

    @Config(desc = USER)
    @NotNull
    private String user = "admin";

    @Config(desc = PASSWORD)
    @NotNull
    private String password = "admin";

    @Config(desc = URL)
    @NotNull
    private String url = "jdbc:derby:memory:westty;create=true";

    @Config(desc = DRIVER)
    @NotNull
    private String driver = "org.apache.derby.jdbc.EmbeddedDriver";

    @Config(desc = PROVIDER)
    @NotNull
    private String provider = "org.hibernate.ejb.HibernatePersistence";

    @Config(desc = TX_TYPE)
    @NotNull
    private String txType = "RESOURCE_LOCAL";

    @Config(desc = HIB_DDL)
    private String hibDdl = "validate";

    @Config(desc = HIB_SHOW_SQL)
    private Boolean hibShowSql = true;

    @Config(desc = HIB_DIALECT)
    private String hibDialect = "org.hibernate.dialect.DerbyDialect";

    public String getId() {
        return ID;
    }

    public String getProvider() {
        return provider;
    }

    public String getTxType() {
        return txType;
    }

    public String getHibDdl() {
        return hibDdl;
    }

    public boolean isHibShowSql() {
        return hibShowSql;
    }

    public String getHibDialect() {
        return hibDialect;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getUrl() {
        return url;
    }

    public String getDriver() {
        return driver;
    }

    public Properties getProperties() {
        Properties props = new Properties();
        props.put(USER, user);
        props.put(PASSWORD, user);
        props.put(URL, url);
        props.put(DRIVER, driver);
        props.put(PROVIDER, provider);
        props.put(TX_TYPE, txType);
        props.put(HIB_DDL, hibDdl);
        props.put(HIB_SHOW_SQL, hibShowSql);
        props.put(HIB_DIALECT, hibDialect);
        return props;
    }

    public Class<?> loadDriver() {
        try {
            Class.forName(driver);
            return Thread.currentThread().getContextClassLoader().loadClass(driver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void install() {
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            List<String> installDdl = Resources.readLines(cl.getResource(INSTALL_DDL),
                    Charsets.UTF_8);
            execute(installDdl, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void exec(File file) {
        try {
            List<String> ddl = Files.readLines(file, Charsets.UTF_8);
            execute(ddl, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void uninstall() {
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            List<String> installDdl = Resources.readLines(cl.getResource(UNINSTALL_DDL),
                    Charsets.UTF_8);
            execute(installDdl, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void dropInstall() {
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            List<String> installDdl = Resources.readLines(cl.getResource(INSTALL_DDL),
                    Charsets.UTF_8);
            List<String> uninstallDdl = Resources.readLines(cl.getResource(UNINSTALL_DDL),
                    Charsets.UTF_8);
            dropInstall(installDdl, uninstallDdl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void dropInstall(List<String> installDdl, List<String> uninstallDdl) {
        try {
            try {
                // Derby does not support support "if exist". 
                // The only option is to ignore SQLException from dropping stuff.
                execute(uninstallDdl, true);
            } catch (SQLException e) {
                // ignore, probably the first DROP TABLE of a non-existing table
            }
            execute(installDdl, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void execute(List<String> commands, boolean ignoreSqlEx) throws SQLException,
            IOException {
        Connection c = getConnection();
        execute(commands, c, ignoreSqlEx);
    }

    public void execute(File file, boolean ignoreSqlEx) throws SQLException, IOException {
        Connection c = getConnection();
        execute(file, c, ignoreSqlEx);
    }

    private Connection getConnection() throws SQLException {
        Properties connectionProps = new Properties();
        connectionProps.put("user", user);
        connectionProps.put("password", password);
        Connection conn = DriverManager.getConnection(url, connectionProps);
        conn.setAutoCommit(true);
        return conn;
    }

    private static void execute(File f, Connection c, boolean ignoreSqlEx) throws SQLException,
            IOException {
        try {
            try {
                for (String sql : readLines(f, Charset.defaultCharset())) {
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
