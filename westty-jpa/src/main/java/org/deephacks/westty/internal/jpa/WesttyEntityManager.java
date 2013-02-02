package org.deephacks.westty.internal.jpa;

import java.util.Map;

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

import org.deephacks.tools4j.config.model.ThreadLocalManager;

class WesttyEntityManager implements EntityManager {

    public void persist(Object entity) {
        get().persist(entity);
    }

    public <T> T merge(T entity) {
        return get().merge(entity);
    }

    public void remove(Object entity) {
        get().remove(entity);
    }

    public <T> T find(Class<T> entityClass, Object primaryKey) {
        return get().find(entityClass, primaryKey);
    }

    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        return get().find(entityClass, primaryKey, properties);
    }

    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        return get().find(entityClass, primaryKey, lockMode);
    }

    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode,
            Map<String, Object> properties) {
        return get().find(entityClass, primaryKey, lockMode, properties);
    }

    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        return get().getReference(entityClass, primaryKey);
    }

    public void flush() {
        get().flush();
    }

    public void setFlushMode(FlushModeType flushMode) {
        get().setFlushMode(flushMode);
    }

    public FlushModeType getFlushMode() {
        return get().getFlushMode();
    }

    public void lock(Object entity, LockModeType lockMode) {
        get().lock(entity, lockMode);
    }

    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        get().lock(entity, lockMode, properties);
    }

    public void refresh(Object entity) {
        get().refresh(entity);
    }

    public void refresh(Object entity, Map<String, Object> properties) {
        get().refresh(entity, properties);
    }

    public void refresh(Object entity, LockModeType lockMode) {
        get().refresh(entity, lockMode);
    }

    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        get().refresh(entity, lockMode, properties);
    }

    public void clear() {
        get().clear();
    }

    public void detach(Object entity) {
        get().detach(entity);
    }

    public boolean contains(Object entity) {
        return get().contains(entity);
    }

    public LockModeType getLockMode(Object entity) {
        return get().getLockMode(entity);
    }

    public void setProperty(String propertyName, Object value) {
        get().setProperty(propertyName, value);
    }

    public Map<String, Object> getProperties() {
        return get().getProperties();
    }

    public Query createQuery(String qlString) {
        return get().createQuery(qlString);
    }

    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        return get().createQuery(criteriaQuery);
    }

    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        return get().createQuery(qlString, resultClass);
    }

    public Query createNamedQuery(String name) {
        return get().createNamedQuery(name);
    }

    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        return get().createNamedQuery(name, resultClass);
    }

    public Query createNativeQuery(String sqlString) {
        return get().createNativeQuery(sqlString);
    }

    public Query createNativeQuery(String sqlString, @SuppressWarnings("rawtypes") Class resultClass) {
        return get().createNativeQuery(sqlString, resultClass);
    }

    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        return get().createNativeQuery(sqlString, resultSetMapping);
    }

    public void joinTransaction() {
        get().joinTransaction();
    }

    public <T> T unwrap(Class<T> cls) {
        return get().unwrap(cls);
    }

    public Object getDelegate() {
        return get().getDelegate();
    }

    public void close() {
        get().close();
    }

    public boolean isOpen() {
        return get().isOpen();
    }

    public EntityTransaction getTransaction() {
        return get().getTransaction();
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return get().getEntityManagerFactory();
    }

    public CriteriaBuilder getCriteriaBuilder() {
        return get().getCriteriaBuilder();
    }

    public Metamodel getMetamodel() {
        return get().getMetamodel();
    }

    private EntityManager get() {
        return ThreadLocalManager.peek(EntityManager.class);
    }
}