package org.deephacks.westty.example;

import java.io.File;

import javax.inject.Inject;

import org.deephacks.westty.Westty;
import org.deephacks.westty.properties.WesttyProperties;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.eventbus.EventBus;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class WesttyMain {
    @Inject
    private EventBus bus;

    public static void main(String[] args) throws Throwable {

        WesttyProperties.init(new File("./src/main/resources"));
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        WesttyProperties.setProperty("config.beanmanager",
                "org.deephacks.tools4j.config.internal.core.xml.XmlBeanManager");
        WesttyProperties.setHtmlDir(new File("./src/main/resources/html"));
        Westty westty = new Westty();
        westty.startup();
        WesttyMain main = westty.getInstance(WesttyMain.class);
        Thread.sleep(Integer.MAX_VALUE);
    }

}