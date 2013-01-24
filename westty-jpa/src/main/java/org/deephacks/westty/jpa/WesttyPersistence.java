package org.deephacks.westty.jpa;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import javax.persistence.spi.PersistenceUnitInfo;

import org.deephacks.westty.Locations;
import org.scannotation.AnnotationDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WesttyPersistence {
    private static final Logger log = LoggerFactory.getLogger(WesttyPersistence.class);
    public static final String PERSISTENCE_PROVIDER = "javax.persistence.spi.PeristenceProvider";
    public static final String PERSISTENCE_XML = "META-INF/persistence.xml";

    protected static final Set<PersistenceProvider> providers = new HashSet<PersistenceProvider>();
    /**
     * The JPA specification does not provide an obvious way of 
     * providing modular application jpa units, so we are stuck
     * with a monolithic westty persistence unit for the moment.
     */
    public static final String WESTTY_JPA_UNIT = "westty-jpa-unit";

    public static final File WESTTY_JPA_PROPS = new File(Locations.getConfDir(), "jpa.properties");

    public static boolean isEnabled() {
        if (WESTTY_JPA_PROPS.exists()) {
            return true;
        } else {
            return false;
        }
    }

    public static EntityManagerFactory createEntityManagerFactory() {
        final WesttyPersistenceUnitInfo unit = new WesttyPersistenceUnitInfo(WESTTY_JPA_PROPS);
        final List<Class<?>> entities = getEntities();
        unit.add(entities);
        EntityManagerFactory emf = createEntityManagerFactory(unit);
        log.debug("Created persistence unit with entities {}", entities);
        return emf;
    }

    public static EntityManagerFactory createEntityManagerFactory(PersistenceUnitInfo info) {
        EntityManagerFactory emf = null;
        List<PersistenceProvider> providers = getProviders();
        for (PersistenceProvider provider : providers) {
            emf = provider.createContainerEntityManagerFactory(info, info.getProperties());
            if (emf != null) {
                break;
            }
        }
        if (emf == null) {
            throw new PersistenceException("No Persistence provider for EntityManager named "
                    + info.getPersistenceUnitName());
        }
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
