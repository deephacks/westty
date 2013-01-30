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

import javax.inject.Inject;
import javax.inject.Singleton;
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
import org.deephacks.westty.WesttyProperties;
import org.deephacks.westty.datasource.WesttyDataSource;
import org.deephacks.westty.spi.WesttyModule;
import org.scannotation.AnnotationDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class WesttyJpaModule implements WesttyModule, PersistenceUnitInfo, ThreadLocalScope {
    private static final Logger log = LoggerFactory.getLogger(WesttyJpaModule.class);

    public static final int LOAD_ORDER = 200;

    private static final String PERSISTENCE_XML = "persistence.xml";

    private final WesttyProperties properties;
    private final WesttyJpaProperties jpaProperties;
    private final WesttyDataSource dataSource;

    private List<Class<?>> classes = new ArrayList<Class<?>>();
    private static EntityManagerFactory EMF;

    @Inject
    public WesttyJpaModule(WesttyProperties properties, WesttyDataSource dataSource) {
        this.properties = properties;
        this.jpaProperties = new WesttyJpaProperties(properties);
        this.dataSource = dataSource;
        EMF = createEntityManagerFactory();
    }

    @Override
    public void startup() {

    }

    @Override
    public void shutdown() {
        EMF.close();
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
        classes.add(cls);
    }

    public void add(Class<?>... cls) {
        classes.addAll(Arrays.asList(cls));
    }

    public void add(Collection<Class<?>> cls) {
        classes.addAll(cls);
    }

    @Override
    public String getPersistenceUnitName() {
        return jpaProperties.getJpaUnit();
    }

    @Override
    public String getPersistenceProviderClassName() {
        return jpaProperties.getProvider();
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() {
        return PersistenceUnitTransactionType.valueOf(jpaProperties.getTxType());
    }

    @Override
    public DataSource getJtaDataSource() {
        return null;
    }

    @Override
    public DataSource getNonJtaDataSource() {
        return dataSource;
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
            return properties.getLibDir().toURI().toURL();
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
        return properties.getProperties();
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

    EntityManager createEntityManager() {
        final EntityManager em = EMF.createEntityManager();
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
            emf = provider.createContainerEntityManagerFactory(this, jpaProperties);
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
