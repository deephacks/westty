package org.deephacks.westty.datasource;


import org.deephacks.westty.test.WesttyJUnit4Runner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

@RunWith(WesttyJUnit4Runner.class)
public class DataSourceTest {

    @Inject
    private DataSource dataSource;

    /**
     * Test that DataSource can be injected, statements can be
     * executed and transactions can be committed.
     */
    @Test
    public void test_datasource_connection() throws Exception {
        try (Connection c = dataSource.getConnection()) {
            PreparedStatement prep = c.prepareStatement("CREATE TABLE TEST (ID varchar(255) PRIMARY KEY)");
            prep.execute();
            c.commit();
        }

        try (Connection c = dataSource.getConnection()) {
            String id = "id";
            Statement stmt = c.createStatement();
            stmt.executeUpdate("INSERT INTO TEST VALUES ('" + id + "')");
            ResultSet result = stmt.executeQuery("SELECT * FROM TEST");
            result.next();
            assertThat(result.getString("ID"), is(id));
            c.commit();
        }
     }
}
