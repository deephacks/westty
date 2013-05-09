package org.deephacks.westty.test;

import java.io.File;
import java.net.URL;

import javax.inject.Inject;

import org.deephacks.westty.Westty;
import org.deephacks.westty.properties.WesttyProperties;

public class Main {
    @Inject
    public WesttyProperties props;

    public static void main(String[] args) throws Throwable {
        Westty w = new Westty();
        w.startup();
        Main main = w.getInstance(Main.class);
        URL url = Thread.currentThread().getContextClassLoader().getResource(".");
        WesttyProperties.setHtmlDir(new File(url.getPath()));
        Thread.sleep(100000);
    }

}
