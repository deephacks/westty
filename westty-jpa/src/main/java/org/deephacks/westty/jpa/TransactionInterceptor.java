package org.deephacks.westty.jpa;

import java.io.Serializable;

import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;

import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Transactional
@Interceptor
public class TransactionInterceptor implements Serializable {
    private Logger log = LoggerFactory.getLogger(TransactionInterceptor.class);

    private static final long serialVersionUID = -1033443722024614083L;
    @Inject
    @Any
    private WesttyEntityManagerProvider tlem;

    @AroundInvoke
    public Object aroundInvoke(InvocationContext ic) throws Exception {
        EntityManager em = tlem.createAndRegister();
        Object result = null;
        try {
            em.getTransaction().begin();
            result = ic.proceed();
            em.getTransaction().commit();
        } catch (Exception e) {
            try {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
            } catch (HibernateException e1) {
                log.error("Error rolling back tx. ", e1);
            }
            throw e;
        } finally {
            if (em != null) {
                tlem.unregister(em);
                em.close();
            }
        }
        return result;

    }
}
