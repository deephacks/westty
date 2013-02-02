package org.deephacks.westty.test;

import java.io.File;
import java.net.URL;

import javax.inject.Inject;

import org.deephacks.westty.Westty;
import org.deephacks.westty.properties.WesttyProperties;
import org.deephacks.westty.properties.WesttyPropertyBuilder;

public class Main {
    @Inject
    public WesttyProperties props;

    public static void main(String[] args) throws Throwable {
        Westty w = new Westty();
        w.startup();
        Main main = w.getInstance(Main.class);
        System.out.println(main.props);
        Thread.sleep(100000);
    }

    @WesttyPropertyBuilder(priority = 1)
    public static void build(WesttyProperties properties) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(".");
        properties.setHtmlDir(new File(url.getPath()));
    }

}
