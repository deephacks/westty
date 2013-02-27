package org.deephacks.westty.internal.core.extension;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.inject.Singleton;

import org.deephacks.tools4j.config.Config;
import org.deephacks.tools4j.config.ConfigDefault;
import org.deephacks.westty.internal.core.extension.WesttyCoreExtensionImpl.WesttyCoreExtension;

@Singleton
public class WesttyConfigBootstrap extends WesttyCoreExtension {
    private static final Set<Class<?>> schemas = new HashSet<Class<?>>();
    private static final Set<Object> defaults = new HashSet<Object>();

    public <X> void processAnnotatedType(@Observes ProcessAnnotatedType<X> pat) {
        AnnotatedType<?> type = pat.getAnnotatedType();
        if (type.isAnnotationPresent(Config.class)) {
            schemas.add(pat.getAnnotatedType().getJavaClass());
        }
        for (AnnotatedMethod<?> m : type.getMethods()) {
            ConfigDefault def = m.getAnnotation(ConfigDefault.class);
            if (def != null && m.isStatic()) {
                Method method = m.getJavaMember();
                try {
                    Object o = method.invoke(null, (Object[]) null);
                    if (o instanceof List) {
                        List<?> list = (List<?>) o;
                        for (Object obj : list) {
                            defaults.add(obj);
                        }
                    } else {
                        defaults.add(o);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public Class<?>[] getSchemas() {
        return schemas.toArray(new Class<?>[0]);
    }

    public Object[] getDefaults() {
        return defaults.toArray(new Object[0]);
    }

}
