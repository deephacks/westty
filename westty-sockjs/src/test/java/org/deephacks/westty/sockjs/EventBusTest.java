package org.deephacks.westty.sockjs;

import java.io.File;
import java.net.URL;

import javax.inject.Inject;

import org.deephacks.westty.Westty;
import org.deephacks.westty.properties.WesttyProperties;
import org.deephacks.westty.properties.WesttyPropertyBuilder;

public class EventBusTest {

    @Inject
    public WesttyProperties props;

    public static void main(String[] args) throws Throwable {
        Westty w = new Westty();
        w.startup();
        EventBusTest main = w.getInstance(EventBusTest.class);
        Thread.sleep(100000);
    }

    @WesttyPropertyBuilder(priority = 1)
    public static void build(WesttyProperties properties) {
        WesttySockJsProperties p = new WesttySockJsProperties(new WesttyProperties());
        p.setProperty("westty.cluster.ips", "192.168.0.100, 192.168.0.101");
        URL url = Thread.currentThread().getContextClassLoader().getResource(".");
        properties.setHtmlDir(new File(url.getPath()));
    }

}
