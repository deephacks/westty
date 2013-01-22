package org.deephacks.westty.jpa;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;
import java.util.Stack;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.deephacks.tools4j.config.internal.core.jpa.EntityManagerProvider;
import org.deephacks.westty.Locations;

@ApplicationScoped
public class WesttyEntityManagerProvider implements Serializable, EntityManagerProvider {
    /**
     * The JPA specification does not provide an obvious way of 
     * providing modular application jpa units, so we are stuck
     * with a monolithic westty persistence unit for the moment.
     */
    public static final String WESTTY_JPA_UNIT = "westty-jpa-unit";

    public static final File JPA_PROPS = new File(Locations.CONF_DIR, "jpa.properties");

    private static final long serialVersionUID = -3337029214056469814L;

    private EntityManagerFactory emf;

    private ThreadLocal<Stack<EntityManager>> emStackThreadLocal = new ThreadLocal<Stack<EntityManager>>();
    public static final WesttyEntityManagerProvider instance = new WesttyEntityManagerProvider();

    private WesttyEntityManagerProvider() {

    }

    public EntityManager get() {
        final Stack<EntityManager> entityManagerStack = emStackThreadLocal.get();
        if (entityManagerStack == null || entityManagerStack.isEmpty()) {
            return null;
        } else {
            return entityManagerStack.peek();
        }
    }

    public EntityManager createAndRegister() {
        if (emf == null) {
            Properties p = new Properties();
            try {
                p.load(new FileInputStream(JPA_PROPS));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            emf = Persistence.createEntityManagerFactory(WESTTY_JPA_UNIT, p);
        }
        Stack<EntityManager> entityManagerStack = emStackThreadLocal.get();
        if (entityManagerStack == null) {
            entityManagerStack = new Stack<EntityManager>();
            emStackThreadLocal.set(entityManagerStack);
        }

        final EntityManager entityManager = emf.createEntityManager();
        entityManagerStack.push(entityManager);
        return entityManager;
    }

    public void unregister(EntityManager entityManager) {
        final Stack<EntityManager> entityManagerStack = emStackThreadLocal.get();
        if (entityManagerStack == null || entityManagerStack.isEmpty())
            throw new IllegalStateException(
                    "Removing of entity manager failed. Your entity manager was not found.");

        if (entityManagerStack.peek() != entityManager)
            throw new IllegalStateException(
                    "Removing of entity manager failed. Your entity manager was not found.");
        entityManagerStack.pop();
    }

    @Override
    public EntityManager getEntityManager() {
        return get();
    }
}
