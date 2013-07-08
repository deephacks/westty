package org.deephacks.westty.config;

import org.deephacks.tools4j.config.RuntimeContext;
import org.deephacks.tools4j.config.model.AbortRuntimeException;
import org.deephacks.tools4j.config.model.Events;
import org.deephacks.westty.server.ServerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * This class enables injection of configurable classes that have an @Id
 * same as the running server instance. This also means that the configurable
 * class must have a constructor that takes its id as an argument.
 *
 * @param <T> configurable class
 */
public class ServerSpecificConfigProxy<T> {
    private T config;

    private ServerSpecificConfigProxy(T config){
        this.config = config;
    }

    /**
     * @return The configurable server specific instance or an empty default
     * instance if it does not exist in the configuration bean manager.
     */
    public T get(){
        return config;
    }

    static class ServerSpecificConfigProxyProducer<T> {
        private static final Logger log = LoggerFactory.getLogger(ServerSpecificConfigProxyProducer.class);
        @Inject
        private ServerName serverName;

        @Inject
        private RuntimeContext ctx;

        @Produces
        public ServerSpecificConfigProxy<T> produceServerConfigProxy(InjectionPoint ip){
            Class<?> declaringClass = ip.getMember().getDeclaringClass();
            Class<?> configCls = getParameterizedType(declaringClass, ip.getAnnotated().getBaseType());
            try {
                Object config = ctx.get(serverName.getName(), configCls);
                return new ServerSpecificConfigProxy(config);
            } catch (AbortRuntimeException e) {
                if (e.getEvent().getCode() != Events.CFG304){
                    throw e;
                }
                log.debug("Config instance {} of type {} not found", serverName.getName(), configCls);
                Object config = newInstance(configCls, serverName.getName());
                return new ServerSpecificConfigProxy(config);
            }
        }

        Class<?> getParameterizedType(Class<?> cls, final Type type) {
            if (!ParameterizedType.class.isAssignableFrom(type.getClass())) {
                throw new IllegalArgumentException("ServerSpecificConfigProxy does not have generic type " + type);
            }

            ParameterizedType ptype = (ParameterizedType) type;
            Type[] targs = ptype.getActualTypeArguments();

            for (Type aType : targs) {
                return extractClass(cls, aType);
            }
            throw new IllegalArgumentException("Could not get generic type from ServerSpecificConfigProxy " + type);
        }
        private static Class<?> extractClass(Class<?> ownerClass, Type arg) {
            if (arg instanceof ParameterizedType) {
                return extractClass(ownerClass, ((ParameterizedType) arg).getRawType());
            } else if (arg instanceof GenericArrayType) {
                throw new UnsupportedOperationException("GenericArray types are not supported.");
            } else if (arg instanceof TypeVariable) {
                throw new UnsupportedOperationException("GenericArray types are not supported.");
            }
            return (arg instanceof Class ? (Class<?>) arg : Object.class);
        }

        Object newInstance(Class<?> type, String name) {
            try {
                Class<?> enclosing = type.getEnclosingClass();
                if (enclosing == null) {
                    Constructor<?> c = type.getDeclaredConstructor(String.class);
                    c.setAccessible(true);
                    return type.cast(c.newInstance(name));
                }
                Object o = enclosing.newInstance();
                Constructor<?> cc = type.getDeclaredConstructor(enclosing, String.class);
                cc.setAccessible(true);
                return type.cast(cc.newInstance(o, name));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        static Class<?> forName(String className) {
            try {
                return Thread.currentThread().getContextClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }


    }
}
