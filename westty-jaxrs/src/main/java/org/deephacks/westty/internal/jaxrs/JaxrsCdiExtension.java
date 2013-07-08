package org.deephacks.westty.internal.jaxrs;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.inject.Singleton;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class JaxrsCdiExtension implements Extension {
    private static Set<Class<?>> jaxrsClasses = new HashSet<>();

    <X> void processAnnotatedType(@Observes ProcessAnnotatedType<X> pat) {
        AnnotatedType<X> type = pat.getAnnotatedType();
        if (!type.isAnnotationPresent(Path.class) && !type.isAnnotationPresent(Provider.class)) {
            return;
        }

        Class<?> cls = pat.getAnnotatedType().getJavaClass();
        jaxrsClasses.add(cls);
    }

    public Set<Class<?>> getJaxrsClasses() {
        return jaxrsClasses;
    }
}
