package org.deephacks.westty.job;

import java.io.IOException;
import java.sql.SQLException;

import org.deephacks.westty.test.SQLExec;
import org.deephacks.westty.test.WesttyJUnit4Runner;
import org.deephacks.westty.test.WesttyTestBootstrap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

@RunWith(WesttyJUnit4Runner.class)
public class JobTest {

    @WesttyTestBootstrap
    private static void bootstrap() throws SQLException, IOException {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        SQLExec bootstrap = new SQLExec("westty", "westty", "jdbc:derby:memory:westty;create=true");
        bootstrap.executeResource("META-INF/install_job_derby.ddl", false);
    }

    @Before
    public void before() throws Exception {

    }

    public void after() {

    }

    @Test
    public void test() throws Exception {
        Thread.sleep(1000);
    }

}
