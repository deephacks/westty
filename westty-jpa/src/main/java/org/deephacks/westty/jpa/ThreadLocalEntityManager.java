package org.deephacks.westty.jpa;

import java.io.Serializable;
import java.util.Stack;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.deephacks.westty.config.JpaConfig;

@ApplicationScoped
public class ThreadLocalEntityManager implements Serializable {

    private static final long serialVersionUID = -3337029214056469814L;

    private EntityManagerFactory emf;

    @Inject
    private JpaConfig config;

    private ThreadLocal<Stack<EntityManager>> emStackThreadLocal = new ThreadLocal<Stack<EntityManager>>();

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
            emf = Persistence.createEntityManagerFactory(config.getUnitName(),
                    config.getProperties());
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
}
