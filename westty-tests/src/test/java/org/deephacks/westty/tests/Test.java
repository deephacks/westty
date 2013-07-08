package org.deephacks.westty.tests;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.deephacks.westty.Westty;
import org.deephacks.westty.datasource.DataSourceProperties;
import org.deephacks.westty.test.SQLExec;
import org.slf4j.LoggerFactory;

public class Test {
    public static void main(String[] args) throws Throwable {
        /*
        Weld w = new Weld();
        w.initialize();
        w.shutdown();
        System.out.println("DONWE");
        */
        test();
    }

    public static void test() throws Throwable {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        DataSourceProperties ds = new DataSourceProperties();
        SQLExec sql = new SQLExec(ds.getUsername(), ds.getPassword(), ds.getUrl());
        sql.executeResource("META-INF/install_config_derby.ddl", false);
        Westty w = new Westty();
        w.startup();
        System.out.println("STARTED ------------------- ");
        Thread.sleep(1000);
        w.shutdown();
        System.out.println("SHUTDOWN ------------------- ");
        Thread.sleep(1000);
        w = new Westty();
        System.out.println("STARTING ------------------- ");
        w.startup();
        System.out.println("SHUTDOWN ------------------- ");
    }

}
