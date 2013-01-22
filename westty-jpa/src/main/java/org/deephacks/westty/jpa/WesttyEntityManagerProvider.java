package org.deephacks.westty.jpa;

import java.io.Serializable;
import java.util.Stack;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.deephacks.tools4j.config.internal.core.jpa.EntityManagerProvider;

public class WesttyEntityManagerProvider extends EntityManagerProvider implements Serializable {

    private static final long serialVersionUID = -3337029214056469814L;

    private EntityManagerFactory emf;

    private static final ThreadLocal<Stack<EntityManager>> emStackThreadLocal = new ThreadLocal<Stack<EntityManager>>();
    private static final WesttyEntityManagerProvider instance = new WesttyEntityManagerProvider();

    public static WesttyEntityManagerProvider getInstance() {
        return instance;
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
            openProvider();
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

    @Override
    public void openProvider() {
        emf = WesttyPersistence.createEntityManagerFactory();
    }

    @Override
    public void closeProvider() {
        emf.close();
    }

}
