package org.deephacks.westty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;

import com.google.common.base.Strings;
import com.google.common.io.Closeables;

@Alternative
public class WesttyProperties {
    private static final long serialVersionUID = -680769546926292449L;
    private static final String LIB_DIR_PROP = "westty.lib.dir";
    private static final String CONF_DIR_PROP = "westty.conf.dir";
    private static final String BIN_DIR_PROP = "westty.bin.dir";
    private static final String HTML_DIR_PROP = "westty.html.dir";

    private Properties properties = new Properties();

    private WesttyProperties() {
    }

    static WesttyProperties create() {
        return new WesttyProperties();
    }

    public void add(Properties prop) {
        for (String key : prop.stringPropertyNames()) {
            properties.setProperty(key, prop.getProperty(key));
        }
    }

    public void add(WesttyProperties prop) {
        add(prop.getProperties());
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

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getPropertyOrFail(String prop) {
        String value = getProperty(prop);
        if (Strings.isNullOrEmpty(value)) {
            throw new IllegalArgumentException(prop + " property missing.");
        }
        return value;
    }

    public void setLibDir(File dir) {
        properties.setProperty(LIB_DIR_PROP, dir.getAbsolutePath());
    }

    public File getLibDir() {
        return new File(properties.getProperty(LIB_DIR_PROP));
    }

    public void setConfDir(File dir) {
        properties.setProperty(CONF_DIR_PROP, dir.getAbsolutePath());
    }

    public File getConfDir() {
        return new File(properties.getProperty(CONF_DIR_PROP));
    }

    public void setBinDir(File dir) {
        properties.setProperty(BIN_DIR_PROP, dir.getAbsolutePath());
    }

    public File getBinDir() {
        return new File(properties.getProperty(BIN_DIR_PROP));
    }

    public void setHtmlDir(File dir) {
        properties.setProperty(HTML_DIR_PROP, dir.getAbsolutePath());
    }

    public File getHtmlDir() {
        return new File(properties.getProperty(HTML_DIR_PROP));
    }

    @Override
    public String toString() {
        return properties.toString();
    }

    @Singleton
    public static class WesttyDefaultProperties implements WesttyPropertyExtension {
        private static final String WESTTY_ROOT_PROP = "westty.root.dir";
        private static final String WESTTY_ROOT = System.getProperty(WESTTY_ROOT_PROP);
        private static final String WESTTY_PROPERTIES_FILE = "westty.properties";

        private static final String CONF = "conf";
        private static final String LIB = "lib";
        private static final String BIN = "bin";
        private static final String HTML = "html";

        @Override
        public void extendProperties(WesttyProperties properties) {
            properties.setBinDir(new File("/default"));
            properties.setConfDir(new File("/default"));
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
            properties.loadProperties(new File(root, WESTTY_PROPERTIES_FILE));
        }

        @Override
        public int priority() {
            return 0;
        }
    }

}
