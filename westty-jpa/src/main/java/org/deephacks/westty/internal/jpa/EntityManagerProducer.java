package org.deephacks.westty.internal.jpa;

import org.deephacks.confit.model.ThreadLocalManager;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class EntityManagerProducer {

    private EntityManagerFactory emf;

    @Inject
    public EntityManagerProducer(EntityManagerFactory emf) {
        this.emf = emf;
    }

    EntityManager get() {
        return ThreadLocalManager.peek(EntityManager.class);
    }

    EntityManager createEntityManager() {
        final EntityManager em = emf.createEntityManager();
        ThreadLocalManager.push(EntityManager.class, em);
        return em;
    }

    void removeEntityManager() {
        EntityManager em = ThreadLocalManager.pop(EntityManager.class);
        if (em == null)
            throw new IllegalStateException(
                    "Removing of entity manager failed. Your entity manager was not found.");
    }
}
