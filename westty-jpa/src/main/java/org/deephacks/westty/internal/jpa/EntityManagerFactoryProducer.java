package org.deephacks.westty.internal.jpa;

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

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import org.deephacks.westty.jpa.WesttyJpaProperties;
import org.deephacks.westty.properties.WesttyProperties;
import org.scannotation.AnnotationDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityManagerFactoryProducer implements PersistenceUnitInfo {
    public static final String PERSISTENCE_XML = "META-INF/persistence.xml";

    private static final Logger log = LoggerFactory.getLogger(EntityManagerFactoryProducer.class);
    private final WesttyJpaProperties jpaProperties;
    private List<Class<?>> classes = new ArrayList<Class<?>>();
    private DataSource datasource;

    @Inject
    public EntityManagerFactoryProducer(DataSource datasource) {
        this.jpaProperties = new WesttyJpaProperties();
        this.datasource = datasource;
    }

    @Produces
    @Singleton
    public EntityManagerFactory produceEntityManagerFactory() {
        final List<Class<?>> entities = getEntities();
        add(entities);
        EntityManagerFactory emf = null;
        List<PersistenceProvider> providers = getProviders();
        for (PersistenceProvider provider : providers) {
            emf = provider.createContainerEntityManagerFactory(this,
                    WesttyJpaProperties.getProperties());
            if (emf != null) {
                break;
            }
        }
        if (emf == null) {
            log.info("No Persistence provider for EntityManager named " + getPersistenceUnitName());
            return null;
        } else {
            log.debug("Created persistence unit " + getPersistenceUnitName() + " with entities {}",
                    entities);
        }
        return emf;
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
        return datasource;
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
            return WesttyProperties.getLibDir().toURI().toURL();
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
        return ValidationMode.AUTO;
    }

    @Override
    public Properties getProperties() {
        return WesttyProperties.getProperties();
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

    public void add(Class<?> cls) {
        classes.add(cls);
    }

    public void add(Class<?>... cls) {
        classes.addAll(Arrays.asList(cls));
    }

    public void add(Collection<Class<?>> cls) {
        classes.addAll(cls);
    }

    private List<PersistenceProvider> getProviders() {
        return PersistenceProviderResolverHolder.getPersistenceProviderResolver()
                .getPersistenceProviders();
    }

    private List<Class<?>> getEntities() {
        try {
            AnnotationDB db = new AnnotationDB();
            List<Class<?>> entities = new ArrayList<Class<?>>();
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            URL[] jars = getPersistenceArchives(cl);
            db.scanArchives(jars);
            Map<String, Set<String>> annotationIndex = db.getAnnotationIndex();
            Set<String> classes = annotationIndex.get(Entity.class.getName());
            if (classes == null) {
                return entities;
            }
            for (String cls : classes) {
                Class<?> entity = cl.loadClass(cls);
                entities.add(entity);
            }
            return entities;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private URL[] getPersistenceArchives(ClassLoader cl) throws IOException {
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
