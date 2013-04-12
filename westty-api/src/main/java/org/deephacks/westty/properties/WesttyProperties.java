package org.deephacks.westty.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.enterprise.inject.Alternative;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.io.Closeables;

@Alternative
public class WesttyProperties {
    private Logger log = LoggerFactory.getLogger(WesttyProperties.class);

    public static final String LIB_DIR_PROP = "westty.lib.dir";
    public static final String CONF_DIR_PROP = "westty.conf.dir";
    public static final String BIN_DIR_PROP = "westty.bin.dir";
    public static final String HTML_DIR_PROP = "westty.html.dir";

    public static final String PUBLIC_IP_PROP = "westty.public_ip";
    public static final String PRIVATE_IP_PROP = "westty.private_ip";

    public static final String WESTTY_ROOT_PROP = "westty.root.dir";
    public static final String WESTTY_ROOT = System.getProperty(WESTTY_ROOT_PROP);
    public static final String WESTTY_PROPERTIES_FILE = "westty.properties";

    public static final String CONF = "conf";
    public static final String LIB = "lib";
    public static final String BIN = "bin";
    public static final String HTML = "html";

    private final Properties properties;

    private String hostAddress;

    public WesttyProperties() {
        properties = new Properties();
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            hostAddress = "0.0.0.0";
        }
    }

    public WesttyProperties(WesttyProperties properties) {
        this.properties = properties.getProperties();
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            hostAddress = "0.0.0.0";
        }
    }

    @WesttyPropertyBuilder
    public static void build(WesttyProperties properties) {
        if (Strings.isNullOrEmpty(WESTTY_ROOT)) {
            return;
        }
        File root = new File(WESTTY_ROOT);
        if (!root.exists()) {
            return;
        }
        properties.setBinDir(new File(root, BIN));
        properties.setLibDir(new File(root, LIB));
        properties.setConfDir(new File(root, CONF));
        properties.setHtmlDir(new File(root, HTML));
        properties.loadProperties(new File(properties.getConfDir(), WESTTY_PROPERTIES_FILE));
    }

    public void add(Properties prop) {
        for (String key : prop.stringPropertyNames()) {
            setProperty(key, prop.getProperty(key));
        }
    }

    public void loadProperties(File file) {
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

    public Properties getProperties() {
        return properties;
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public void setLibDir(File dir) {
        setProperty(LIB_DIR_PROP, dir.getAbsolutePath());
    }

    public File getLibDir() {
        return new File(getPropertyOrEmpty(LIB_DIR_PROP));
    }

    public void setConfDir(File dir) {
        setProperty(CONF_DIR_PROP, dir.getAbsolutePath());
    }

    public File getConfDir() {
        return new File(getPropertyOrEmpty(CONF_DIR_PROP));
    }

    public void setBinDir(File dir) {
        setProperty(BIN_DIR_PROP, dir.getAbsolutePath());
    }

    public File getBinDir() {
        return new File(getPropertyOrEmpty(BIN_DIR_PROP));
    }

    public void setHtmlDir(File dir) {
        setProperty(HTML_DIR_PROP, dir.getAbsolutePath());
    }

    public File getHtmlDir() {
        return new File(getProperty(HTML_DIR_PROP));
    }

    public void setPublicIp(String ip) {
        setProperty(PUBLIC_IP_PROP, ip);
    }

    public String getPublicIp() {
        String value = getProperty(PUBLIC_IP_PROP);
        if (!Strings.isNullOrEmpty(value)) {
            return value;
        }
        return hostAddress;
    }

    public void setPrivateIp(String ip) {
        setProperty(PRIVATE_IP_PROP, ip);
    }

    public String getPrivateIp() {
        String value = getProperty(PRIVATE_IP_PROP);
        if (!Strings.isNullOrEmpty(value)) {
            return value;
        }
        return getPublicIp();
    }

    private String getPropertyOrEmpty(String key) {
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
