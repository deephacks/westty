package org.deephacks.westty;

import com.google.common.base.Strings;
import com.google.common.io.Closeables;
import org.deephacks.tools4j.config.model.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Singleton
public class WesttyProperties {
    private static final Logger log = LoggerFactory.getLogger(WesttyProperties.class);


    public static final String WESTTY_PROPERTIES_FILE = "westty.properties";


    private static final Properties properties = new Properties();

    public WesttyProperties() {
    }

    public static void add(Properties prop) {
        properties.putAll(prop);
    }

    public static void loadProperties(File file) {
        Properties props = new Properties();
        if (!file.exists()) {
            return;
        }
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            props.load(in);
            add(props);
            log.info("Loaded WesttyProperties from file " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            Closeables.closeQuietly(in);
        }
    }

    @Produces
    @Singleton
    public static SystemProperties produceSystemProperties() {
        SystemProperties props = SystemProperties.createDefault();
        SystemProperties.add(properties);
        return props;
    }

    public static Properties getProperties() {
        return properties;
    }

    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public static void setPropertyIfNotSet(String key, String value) {
        Object o = properties.get(key);
        if (o != null) {
            return;
        }
        properties.setProperty(key, value);
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }


    public static String getPropertyOrEmpty(String key) {
        String value = getProperty(key);
        if (Strings.isNullOrEmpty(value)) {
            return "";
        }
        return value;
    }

    @Override
    public String toString() {
        return properties.toString();
    }

}
