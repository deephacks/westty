package org.deephacks.westty.jpa;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.PersistenceUtil;
import javax.persistence.spi.LoadState;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import javax.persistence.spi.PersistenceUnitInfo;

import org.deephacks.tools4j.config.internal.core.jpa.JpaBean;
import org.deephacks.tools4j.config.internal.core.jpa.JpaBeanPk;
import org.deephacks.tools4j.config.internal.core.jpa.JpaBeanSingleton;
import org.deephacks.tools4j.config.internal.core.jpa.JpaProperty;
import org.deephacks.tools4j.config.internal.core.jpa.JpaPropertyPk;
import org.deephacks.tools4j.config.internal.core.jpa.JpaRef;
import org.deephacks.westty.Locations;

public class WesttyPersistence {

    public static final String PERSISTENCE_PROVIDER = "javax.persistence.spi.PeristenceProvider";

    protected static final Set<PersistenceProvider> providers = new HashSet<PersistenceProvider>();
    /**
     * The JPA specification does not provide an obvious way of 
     * providing modular application jpa units, so we are stuck
     * with a monolithic westty persistence unit for the moment.
     */
    public static final String WESTTY_JPA_UNIT = "westty-jpa-unit";

    public static final File WESTTY_JPA_PROPS = new File(Locations.CONF_DIR, "jpa.properties");

    public static boolean isEnabled() {
        if (WESTTY_JPA_PROPS.exists()) {
            return true;
        } else {
            return false;
        }
    }

    public static EntityManagerFactory createEntityManagerFactory() {
        WesttyPersistenceUnitInfo unit = new WesttyPersistenceUnitInfo(WESTTY_JPA_PROPS);
        unit.add(JpaProperty.class, JpaPropertyPk.class, JpaBean.class, JpaBeanPk.class,
                JpaBeanSingleton.class, JpaRef.class);
        return createEntityManagerFactory(unit);
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

    /**
     * @return Returns a <code>PersistenceUtil</code> instance.
     */
    public static PersistenceUtil getPersistenceUtil() {
        return util;
    }

    private static PersistenceUtil util =

    new PersistenceUtil() {
        public boolean isLoaded(Object entity, String attributeName) {
            List<PersistenceProvider> providers = WesttyPersistence.getProviders();
            for (PersistenceProvider provider : providers) {
                final LoadState state = provider.getProviderUtil().isLoadedWithoutReference(entity,
                        attributeName);
                if (state == LoadState.UNKNOWN)
                    continue;
                return state == LoadState.LOADED;
            }
            for (PersistenceProvider provider : providers) {
                final LoadState state = provider.getProviderUtil().isLoadedWithReference(entity,
                        attributeName);
                if (state == LoadState.UNKNOWN)
                    continue;
                return state == LoadState.LOADED;
            }
            return true;
        }

        public boolean isLoaded(Object object) {
            List<PersistenceProvider> providers = WesttyPersistence.getProviders();
            for (PersistenceProvider provider : providers) {
                final LoadState state = provider.getProviderUtil().isLoaded(object);
                if (state == LoadState.UNKNOWN)
                    continue;
                return state == LoadState.LOADED;
            }
            return true;
        }
    };

}
