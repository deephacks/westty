/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.deephacks.westty.internal.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.util.AnnotationLiteral;
import javax.ws.rs.Path;

public class WesttyJaxrsExtension implements Extension {
    private Set<Class<?>> jaxrsClasses = new HashSet<Class<?>>();

    <X> void processAnnotatedType(@Observes ProcessAnnotatedType<X> pat) {

        if (!pat.getAnnotatedType().isAnnotationPresent(Path.class)) {
            return;
        }
        final AnnotatedType<X> org = pat.getAnnotatedType();
        AnnotatedType<X> wrapped = new AnnotatedType<X>() {

            @Override
            public Type getBaseType() {
                return org.getBaseType();
            }

            @Override
            public Set<Type> getTypeClosure() {
                return org.getTypeClosure();
            }

            @Override
            public <T extends Annotation> T getAnnotation(final Class<T> annotation) {
                if (Path.class.equals(annotation)) {
                    class PathLiteral extends AnnotationLiteral<Path> implements Path {

                        @Override
                        public String value() {
                            Path p = (Path) org.getAnnotation(annotation);
                            return "MY/" + p.value();
                        }

                    }
                    return (T) new PathLiteral();
                } else {
                    return org.getAnnotation(annotation);
                }

            }

            @Override
            public Set<Annotation> getAnnotations() {
                HashSet<Annotation> a = new HashSet<Annotation>();
                for (final Annotation annotation2 : org.getAnnotations()) {
                    if (Path.class.equals(annotation2.annotationType())) {
                        class PathLiteral extends AnnotationLiteral<Path> implements Path {

                            @Override
                            public String value() {
                                Path p = (Path) annotation2;
                                return "westty-jaxrs/" + p.value();
                            }

                        }
                        a.add(new PathLiteral());
                    } else {
                        a.add(annotation2);
                    }
                }
                return a;
            }

            @Override
            public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
                return org.isAnnotationPresent(annotationType);
            }

            @Override
            public Class<X> getJavaClass() {
                return org.getJavaClass();
            }

            @Override
            public Set<AnnotatedConstructor<X>> getConstructors() {
                return org.getConstructors();
            }

            @Override
            public Set<AnnotatedMethod<? super X>> getMethods() {
                return org.getMethods();
            }

            @Override
            public Set<AnnotatedField<? super X>> getFields() {
                return org.getFields();
            }

        };
        pat.setAnnotatedType(wrapped);
        Class<?> cls = pat.getAnnotatedType().getJavaClass();
        jaxrsClasses.add(cls);

    }

    public Set<Class<?>> getJaxrsClasses() {
        return jaxrsClasses;
    }
}
