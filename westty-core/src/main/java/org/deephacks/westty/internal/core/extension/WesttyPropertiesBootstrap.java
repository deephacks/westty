package org.deephacks.westty.internal.core.extension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Singleton;

import org.deephacks.westty.internal.core.extension.WesttyCoreExtensionImpl.WesttyCoreExtension;
import org.deephacks.westty.properties.WesttyProperties;
import org.deephacks.westty.properties.WesttyPropertyBuilder;

import com.google.common.collect.Lists;

@Singleton
public class WesttyPropertiesBootstrap extends WesttyCoreExtension {
    private static final Map<Method, WesttyPropertyBuilder> builders = new HashMap<Method, WesttyPropertyBuilder>();

    @Override
    public void afterBeanDiscovery(AfterBeanDiscovery abd, BeanManager bm) {
        abd.addBean(createWesttyProperties(bm));
    }

    @Override
    public <X> void processAnnotatedType(ProcessAnnotatedType<X> pat) {
        AnnotatedType<?> type = pat.getAnnotatedType();
        for (Method m : type.getJavaClass().getDeclaredMethods()) {
            m.setAccessible(true);
            WesttyPropertyBuilder anno = m.getAnnotation(WesttyPropertyBuilder.class);

            if (anno != null) {
                validateProperties(m);
                builders.put(m, anno);
            }
        }
    }

    private void validateProperties(Method m) throws IllegalStateException {
        StringBuilder errors = new StringBuilder();
        if (!Modifier.isStatic(m.getModifiers())) {
            errors.append("WesttyPropertyProducer method [" + m + "] must be static. ");
        }
        Class<?>[] params = m.getParameterTypes();
        if (params.length != 1 || !WesttyProperties.class.isAssignableFrom(params[0])) {
            errors.append("WesttyPropertyProducer method [" + m
                    + "] must have only one argument of type WesttyProperties.");
        }
        if (errors.length() > 0) {
            throw new IllegalStateException(errors.toString());
        }
    }

    private static ArrayList<Method> sortPropertyBuilders() {
        ArrayList<Method> result = Lists.newArrayList(builders.keySet());
        Collections.sort(result, new Comparator<Method>() {

            @Override
            public int compare(Method m1, Method m2) {
                WesttyPropertyBuilder b1 = builders.get(m1);
                WesttyPropertyBuilder b2 = builders.get(m2);
                if (b1.priority() > b2.priority()) {
                    return -1;
                }
                return 1;
            }
        });
        return result;
    }

    private static Bean<WesttyProperties> createWesttyProperties(BeanManager bm) {
        AnnotatedType<SecurityManager> at = bm.createAnnotatedType(SecurityManager.class);
        final InjectionTarget<SecurityManager> it = bm.createInjectionTarget(at);
        return new Bean<WesttyProperties>() {

            @Override
            public WesttyProperties create(CreationalContext<WesttyProperties> ctx) {

                ArrayList<Method> list = sortPropertyBuilders();
                WesttyProperties prop = new WesttyProperties();
                for (Method m : list) {
                    try {
                        m.invoke(null, prop);
                    } catch (Exception e) {
                        throw new IllegalStateException("Could not create WesttyProperties", e);
                    }
                }
                return prop;
            }

            @Override
            public void destroy(WesttyProperties instance,
                    CreationalContext<WesttyProperties> creationalContext) {

            }

            @Override
            public Set<Type> getTypes() {
                Set<Type> types = new HashSet<Type>();
                types.add(WesttyProperties.class);
                types.add(Object.class);
                return types;
            }

            @SuppressWarnings("serial")
            @Override
            public Set<Annotation> getQualifiers() {
                Set<Annotation> qualifiers = new HashSet<Annotation>();
                qualifiers.add(new AnnotationLiteral<Default>() {
                });
                qualifiers.add(new AnnotationLiteral<Any>() {
                });
                return qualifiers;
            }

            @Override
            public Class<? extends Annotation> getScope() {
                return Singleton.class;
            }

            @Override
            public String getName() {
                return "westtyProperties";
            }

            @Override
            public Set<Class<? extends Annotation>> getStereotypes() {
                return Collections.emptySet();
            }

            @Override
            public Class<?> getBeanClass() {
                return WesttyProperties.class;
            }

            @Override
            public boolean isAlternative() {
                return false;
            }

            @Override
            public boolean isNullable() {
                return false;
            }

            @Override
            public Set<InjectionPoint> getInjectionPoints() {
                return it.getInjectionPoints();
            }

        };
    }
}
