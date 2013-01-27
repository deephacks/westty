package org.deephacks.westty.jpa;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import org.deephacks.tools4j.config.model.ThreadLocalManager;
import org.deephacks.tools4j.config.model.ThreadLocalScope;
import org.deephacks.westty.Locations;
import org.deephacks.westty.datasource.WesttyDataSourceModule;
import org.deephacks.westty.spi.WesttyModule;
import org.scannotation.AnnotationDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class WesttyJpaModule extends ThreadLocalScope implements WesttyModule, PersistenceUnitInfo {
    public static final int LOAD_ORDER = 200;
    public static final File DEFAULT_PU = new File(Locations.getConfDir(), "persistence.xml");
    public static final String USER = "javax.persistence.jdbc.user";
    public static final String PASSWORD = "javax.persistence.jdbc.password";
    public static final String URL = "javax.persistence.jdbc.url";
    public static final String DRIVER = "javax.persistence.jdbc.driver";
    public static final String PROVIDER = "javax.persistence.provider";
    public static final String TX_TYPE = "javax.persistence.transactionType";
    public static final String JPA_UNIT = "westty.jpa.unit";
    public static final String PERSISTENCE_XML = "META-INF/persistence.xml";

    private static final Logger log = LoggerFactory.getLogger(WesttyJpaModule.class);

    private String unitName;
    private String txType;
    private String provider;
    private static Properties PROPS;
    private static List<Class<?>> CLASSES = new ArrayList<Class<?>>();
    private static EntityManagerFactory emf;

    @Override
    public void startup(Properties props) {
        WesttyJpaModule.PROPS = props;
        this.unitName = getProperty(JPA_UNIT);
        this.txType = getProperty(TX_TYPE);
        this.provider = getProperty(PROVIDER);
        WesttyJpaModule.emf = createEntityManagerFactory();
    }

    private String getProperty(String name) {
        String value = PROPS.getProperty(name);
        if (Strings.isNullOrEmpty(value)) {
            throw new IllegalArgumentException(name + " property is not defined.");
        }
        return value;
    }

    @Override
    public void shutdown() {
        WesttyJpaModule.emf.close();
    }

    @Override
    public int getLoadOrder() {
        return LOAD_ORDER;
    }

    @Override
    public void createScope() {
        createEntityManager();
    }

    @Override
    public void closeScope() {
        removeEntityManager();
    }

    public void add(Class<?> cls) {
        CLASSES.add(cls);
    }

    public void add(Class<?>... cls) {
        CLASSES.addAll(Arrays.asList(cls));
    }

    public void add(Collection<Class<?>> cls) {
        CLASSES.addAll(cls);
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
        return WesttyDataSourceModule.get();
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
        for (Class<?> cls : CLASSES) {
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
        return PROPS;
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

    EntityManager get() {
        return ThreadLocalManager.peek(EntityManager.class);
    }

    static EntityManager createEntityManager() {
        final EntityManager em = emf.createEntityManager();
        ThreadLocalManager.push(EntityManager.class, em);
        return em;
    }

    static void removeEntityManager() {
        EntityManager em = ThreadLocalManager.pop(EntityManager.class);
        if (em == null)
            throw new IllegalStateException(
                    "Removing of entity manager failed. Your entity manager was not found.");
    }

    private EntityManagerFactory createEntityManagerFactory() {
        final List<Class<?>> entities = getEntities();
        add(entities);
        EntityManagerFactory emf = null;
        List<PersistenceProvider> providers = getProviders();
        for (PersistenceProvider provider : providers) {
            emf = provider.createContainerEntityManagerFactory(this, getJpaProperties());
            if (emf != null) {
                break;
            }
        }
        if (emf == null) {
            throw new PersistenceException("No Persistence provider for EntityManager named "
                    + getPersistenceUnitName());
        }
        log.debug("Created persistence unit with entities {}", entities);
        return emf;
    }

    private Properties getJpaProperties() {
        Properties jpa = new Properties();
        jpa.setProperty(USER, WesttyDataSourceModule.getUsername());
        jpa.setProperty(PASSWORD, WesttyDataSourceModule.getPassword());
        jpa.setProperty(URL, WesttyDataSourceModule.getUrl());
        jpa.setProperty(DRIVER, WesttyDataSourceModule.getDriver());
        jpa.setProperty(TX_TYPE, txType);
        jpa.setProperty(PROVIDER, provider);
        return jpa;

    }

    private static List<PersistenceProvider> getProviders() {
        return PersistenceProviderResolverHolder.getPersistenceProviderResolver()
                .getPersistenceProviders();
    }

    private static List<Class<?>> getEntities() {
        try {
            AnnotationDB db = new AnnotationDB();
            List<Class<?>> entities = new ArrayList<Class<?>>();
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            URL[] jars = getPersistenceArchives(cl);
            db.scanArchives(jars);
            Map<String, Set<String>> annotationIndex = db.getAnnotationIndex();
            for (String cls : annotationIndex.get(Entity.class.getName())) {
                Class<?> entity = cl.loadClass(cls);
                entities.add(entity);
            }
            return entities;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static URL[] getPersistenceArchives(ClassLoader cl) throws IOException {
        final List<URL> result = new ArrayList<URL>();
        final Enumeration<URL> urls = cl.getResources(PERSISTENCE_XML);
        while (urls.hasMoreElements()) {
            // either a jar or file in a dir
            URL url = urls.nextElement();
            File file = new File(url.getFile());
            if (file.exists()) {
                // navigate on directory above META-INF
                url = file.getParentFile().getParentFile().toURI().toURL();
            } else {
                url = ((JarURLConnection) url.openConnection()).getJarFileURL();
            }
            result.add(url);
        }
        return result.toArray(new URL[0]);
    }
}
