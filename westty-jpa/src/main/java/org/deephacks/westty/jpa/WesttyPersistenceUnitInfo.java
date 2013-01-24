package org.deephacks.westty.jpa;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import org.deephacks.westty.Locations;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.OutputSupplier;
import com.jolbox.bonecp.BoneCPDataSource;

public class WesttyPersistenceUnitInfo implements PersistenceUnitInfo {
    public static final String WESTTY_JPA_UNIT = "westty-jpa-unit";
    public static final File DEFAULT_PU = new File(Locations.getConfDir(), "persistence.xml");
    public static final String USER = "javax.persistence.jdbc.user";
    public static final String PASSWORD = "javax.persistence.jdbc.password";
    public static final String URL = "javax.persistence.jdbc.url";
    public static final String DRIVER = "javax.persistence.jdbc.driver";
    public static final String PROVIDER = "javax.persistence.provider";
    public static final String TX_TYPE = "javax.persistence.transactionType";

    private final String unitName;
    private final String txType;
    private final String provider;
    private final String user;
    private final String password;
    private final String url;
    private final String driver;
    private final Properties props;
    private final List<Class<?>> classes = new ArrayList<Class<?>>();

    public WesttyPersistenceUnitInfo(String unitName, Properties p) {
        this.unitName = unitName;
        this.txType = checkNotNull(p.get(TX_TYPE)).toString();
        this.provider = checkNotNull(p.get(PROVIDER)).toString();
        this.user = checkNotNull(p.get(USER)).toString();
        this.password = checkNotNull(p.get(PASSWORD)).toString();
        this.url = checkNotNull(p.get(URL)).toString();
        this.driver = checkNotNull(p.get(DRIVER)).toString();

        this.props = p;
    }

    public WesttyPersistenceUnitInfo(String unitName, File jpaPropFile) {
        this(unitName, getProp(jpaPropFile));
    }

    public WesttyPersistenceUnitInfo(File jpaPropFile) {
        this(WESTTY_JPA_UNIT, getProp(jpaPropFile));
    }

    private static Properties getProp(File propFile) {
        if (!propFile.exists()) {
            return new Properties();
        }
        FileInputStream in = null;
        try {
            in = new FileInputStream(propFile);
            Properties p = new Properties();
            p.load(in);
            return p;
        } catch (IOException e) {
            Closeables.closeQuietly(in);
            return new Properties();
        }
    }

    public void add(Class<?> cls) {
        classes.add(cls);
    }

    public void add(Class<?>... cls) {
        classes.addAll(Arrays.asList(cls));
    }

    public void add(Collection<Class<?>> cls) {
        classes.addAll(cls);
    }

    public void write(File file) throws IOException {
        OutputSupplier<OutputStreamWriter> out = Files.newWriterSupplier(file, Charsets.UTF_8);
        CharStreams.write(toString(), out);
    }

    public void write() throws IOException {
        write(DEFAULT_PU);
    }

    public void load() {
        URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class<URLClassLoader> classLoaderClass = URLClassLoader.class;
        try {
            Method method = classLoaderClass.getDeclaredMethod("addURL", new Class[] { URL.class });
            method.setAccessible(true);
            System.out.println(DEFAULT_PU.toURI().toURL());
            method.invoke(systemClassLoader, new Object[] { DEFAULT_PU.toURI().toURL() });
        } catch (Throwable e) {
            throw new RuntimeException(e);

        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<persistence-unit name=\"" + unitName + "\"");
        if (txType != null) {
            sb.append(" transaction-type=\"" + txType + "\"");
        }
        sb.append(">\n");

        if (provider != null) {
            sb.append("<provider>" + provider + "</provider>\n");
        }

        if (classes != null) {
            for (Class<?> cls : classes) {
                sb.append("<class>" + cls.getCanonicalName() + "</class>\n");
            }
        }

        // Add properties
        if (props != null) {
            sb.append("<properties>\n");
            for (Object key : props.keySet()) {
                Object val = props.get(key);
                sb.append("<property name=\"" + key + "\" value=\"" + val + "\"/>\n");
            }
            sb.append("</properties>\n");
        }

        sb.append("<exclude-unlisted-classes>true</exclude-unlisted-classes>\n");
        sb.append("</persistence-unit>\n");
        return sb.toString();

    }

    @Override
    public String getPersistenceUnitName() {
        return unitName;
    }

    @Override
    public String getPersistenceProviderClassName() {
        return provider;
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() {
        return PersistenceUnitTransactionType.valueOf(txType);
    }

    @Override
    public DataSource getJtaDataSource() {
        return null;
    }

    @Override
    public DataSource getNonJtaDataSource() {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        BoneCPDataSource ds = new BoneCPDataSource();
        ds.setUser(password);
        ds.setPassword(password);
        ds.setJdbcUrl(url);
        WesttyDataSource wds = new WesttyDataSource(ds);
        return wds;
    }

    @Override
    public List<String> getMappingFileNames() {
        return null;
    }

    @Override
    public List<URL> getJarFileUrls() {
        return new ArrayList<URL>();
    }

    @Override
    public java.net.URL getPersistenceUnitRootUrl() {
        try {
            return Locations.getLibDir().toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getManagedClassNames() {
        List<String> classNames = new ArrayList<String>();
        for (Class<?> cls : classes) {
            classNames.add(cls.getCanonicalName());
        }
        return classNames;
    }

    @Override
    public boolean excludeUnlistedClasses() {
        return true;
    }

    @Override
    public SharedCacheMode getSharedCacheMode() {
        return SharedCacheMode.NONE;
    }

    @Override
    public ValidationMode getValidationMode() {
        return ValidationMode.NONE;
    }

    @Override
    public Properties getProperties() {
        return props;
    }

    @Override
    public String getPersistenceXMLSchemaVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    @Override
    public void addTransformer(ClassTransformer transformer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassLoader getNewTempClassLoader() {
        return null;
    }

}
