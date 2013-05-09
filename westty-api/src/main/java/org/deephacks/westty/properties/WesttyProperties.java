package org.deephacks.westty.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.deephacks.tools4j.config.model.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.io.Closeables;

@Singleton
public class WesttyProperties {
    private static final Logger log = LoggerFactory.getLogger(WesttyProperties.class);

    public static final String LIB_DIR_PROP = "westty.lib.dir";
    public static final String CONF_DIR_PROP = "westty.conf.dir";
    public static final String BIN_DIR_PROP = "westty.bin.dir";
    public static final String HTML_DIR_PROP = "westty.html.dir";

    public static final String PUBLIC_IP_PROP = "westty.public_ip";
    public static final String PRIVATE_IP_PROP = "westty.private_ip";

    public static final String WESTTY_PROPERTIES_FILE = "westty.properties";

    public static final String CONF = "conf";
    public static final String LIB = "lib";
    public static final String BIN = "bin";
    public static final String HTML = "html";

    private static final Properties properties = new Properties();

    private static String hostAddress = "127.0.0.1";

    public WesttyProperties() {
    }

    public static void init(File root) {
        if (!root.exists()) {
            return;
        }
        setBinDir(new File(root, BIN));
        setLibDir(new File(root, LIB));
        setConfDir(new File(root, CONF));
        setHtmlDir(new File(root, HTML));
        File propFile = new File(getConfDir(), WESTTY_PROPERTIES_FILE);
        loadProperties(propFile);
        SystemProperties.add(getProperties());
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
            return;
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

    public static void setLibDir(File dir) {
        setProperty(LIB_DIR_PROP, dir.getAbsolutePath());
    }

    public static File getLibDir() {
        return new File(getPropertyOrEmpty(LIB_DIR_PROP));
    }

    public static void setConfDir(File dir) {
        setProperty(CONF_DIR_PROP, dir.getAbsolutePath());
    }

    public static File getConfDir() {
        return new File(getPropertyOrEmpty(CONF_DIR_PROP));
    }

    public static void setBinDir(File dir) {
        setProperty(BIN_DIR_PROP, dir.getAbsolutePath());
    }

    public static File getBinDir() {
        return new File(getPropertyOrEmpty(BIN_DIR_PROP));
    }

    public static void setHtmlDir(File dir) {
        setProperty(HTML_DIR_PROP, dir.getAbsolutePath());
    }

    public static File getHtmlDir() {
        return new File(getProperty(HTML_DIR_PROP));
    }

    public static void setPublicIp(String ip) {
        setProperty(PUBLIC_IP_PROP, ip);
    }

    public static String getPublicIp() {
        String value = getProperty(PUBLIC_IP_PROP);
        if (!Strings.isNullOrEmpty(value)) {
            return value;
        }
        return hostAddress;
    }

    public static void setPrivateIp(String ip) {
        setProperty(PRIVATE_IP_PROP, ip);
    }

    public static String getPrivateIp() {
        String value = getProperty(PRIVATE_IP_PROP);
        if (!Strings.isNullOrEmpty(value)) {
            return value;
        }
        return getPublicIp();
    }

    private static String getPropertyOrEmpty(String key) {
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
