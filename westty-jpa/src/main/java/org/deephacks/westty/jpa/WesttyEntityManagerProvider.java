package org.deephacks.westty.jpa;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.deephacks.tools4j.config.model.ThreadLocalManager;
import org.deephacks.tools4j.config.model.ThreadLocalScope;

public class WesttyEntityManagerProvider extends ThreadLocalScope implements Serializable {

    private static final long serialVersionUID = -3337029214056469814L;
    private static EntityManagerFactory emf;

    public WesttyEntityManagerProvider() {
        emf = WesttyPersistence.createEntityManagerFactory();
    }

    public EntityManager get() {
        return ThreadLocalManager.peek(EntityManager.class);
    }

    public EntityManager createEntityManager() {

        final EntityManager em = emf.createEntityManager();
        ThreadLocalManager.push(EntityManager.class, em);
        return em;
    }

    public void closeEntityManagerFactory() {
        emf.close();
    }

    public void removeEntityManager() {
        EntityManager em = ThreadLocalManager.pop(EntityManager.class);
        if (em == null)
            throw new IllegalStateException(
                    "Removing of entity manager failed. Your entity manager was not found.");
    }

    @Override
    public void createScope() {
        createEntityManager();
    }

    @Override
    public void closeScope() {
        removeEntityManager();
    }

}
