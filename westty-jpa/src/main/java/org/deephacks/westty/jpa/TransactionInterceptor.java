package org.deephacks.westty.jpa;

import java.io.Serializable;
import java.util.concurrent.Callable;

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
    private static final Logger log = LoggerFactory.getLogger(TransactionInterceptor.class);

    private static final long serialVersionUID = -1033443722024614083L;

    private static final WesttyEntityManagerProvider tlem = WesttyEntityManagerProvider
            .getInstance();

    @AroundInvoke
    public Object aroundInvoke(final InvocationContext ic) throws Exception {
        return executeInTx(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                return ic.proceed();
            }
        });
    }

    public static Object executeInTx(Callable<?> future) throws Exception {
        EntityManager em = tlem.createAndRegister();
        Object result = null;
        try {
            em.getTransaction().begin();
            result = future.call();
            if (em.getTransaction().isActive()) {
                em.getTransaction().commit();
            }
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
