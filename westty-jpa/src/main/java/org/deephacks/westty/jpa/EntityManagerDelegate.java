package org.deephacks.westty.jpa;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;

@ApplicationScoped
public class EntityManagerDelegate implements EntityManager {

    @Inject
    private ThreadLocalEntityManager tlem;

    public void persist(Object entity) {
        if (tlem == null) {
            System.out.println("tlem null");
        }
        EntityManager em = tlem.get();
        if (em == null) {
            System.out.println("em null");
        }
        tlem.get().persist(entity);
    }

    public <T> T merge(T entity) {
        return tlem.get().merge(entity);
    }

    public void remove(Object entity) {
        tlem.get().remove(entity);
    }

    public <T> T find(Class<T> entityClass, Object primaryKey) {
        return tlem.get().find(entityClass, primaryKey);
    }

    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        return tlem.get().find(entityClass, primaryKey, properties);
    }

    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        return tlem.get().find(entityClass, primaryKey, lockMode);
    }

    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode,
            Map<String, Object> properties) {
        return tlem.get().find(entityClass, primaryKey, lockMode, properties);
    }

    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        return tlem.get().getReference(entityClass, primaryKey);
    }

    public void flush() {
        tlem.get().flush();
    }

    public void setFlushMode(FlushModeType flushMode) {
        tlem.get().setFlushMode(flushMode);
    }

    public FlushModeType getFlushMode() {
        return tlem.get().getFlushMode();
    }

    public void lock(Object entity, LockModeType lockMode) {
        tlem.get().lock(entity, lockMode);
    }

    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        tlem.get().lock(entity, lockMode, properties);
    }

    public void refresh(Object entity) {
        tlem.get().refresh(entity);
    }

    public void refresh(Object entity, Map<String, Object> properties) {
        tlem.get().refresh(entity, properties);
    }

    public void refresh(Object entity, LockModeType lockMode) {
        tlem.get().refresh(entity, lockMode);
    }

    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        tlem.get().refresh(entity, lockMode, properties);
    }

    public void clear() {
        tlem.get().clear();
    }

    public void detach(Object entity) {
        tlem.get().detach(entity);
    }

    public boolean contains(Object entity) {
        return tlem.get().contains(entity);
    }

    public LockModeType getLockMode(Object entity) {
        return tlem.get().getLockMode(entity);
    }

    public void setProperty(String propertyName, Object value) {
        tlem.get().setProperty(propertyName, value);
    }

    public Map<String, Object> getProperties() {
        return tlem.get().getProperties();
    }

    public Query createQuery(String qlString) {
        return tlem.get().createQuery(qlString);
    }

    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        return tlem.get().createQuery(criteriaQuery);
    }

    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        return tlem.get().createQuery(qlString, resultClass);
    }

    public Query createNamedQuery(String name) {
        return tlem.get().createNamedQuery(name);
    }

    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        return tlem.get().createNamedQuery(name, resultClass);
    }

    public Query createNativeQuery(String sqlString) {
        return tlem.get().createNativeQuery(sqlString);
    }

    public Query createNativeQuery(String sqlString, @SuppressWarnings("rawtypes") Class resultClass) {
        return tlem.get().createNativeQuery(sqlString, resultClass);
    }

    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        return tlem.get().createNativeQuery(sqlString, resultSetMapping);
    }

    public void joinTransaction() {
        tlem.get().joinTransaction();
    }

    public <T> T unwrap(Class<T> cls) {
        return tlem.get().unwrap(cls);
    }

    public Object getDelegate() {
        return tlem.get().getDelegate();
    }

    public void close() {
        tlem.get().close();
    }

    public boolean isOpen() {
        return tlem.get().isOpen();
    }

    public EntityTransaction getTransaction() {
        return tlem.get().getTransaction();
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return tlem.get().getEntityManagerFactory();
    }

    public CriteriaBuilder getCriteriaBuilder() {
        return tlem.get().getCriteriaBuilder();
    }

    public Metamodel getMetamodel() {
        return tlem.get().getMetamodel();
    }
}