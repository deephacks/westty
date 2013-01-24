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

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

public class WesttyJaxrsExtension implements Extension {
    private Set<Class<?>> jaxrsClasses = new HashSet<Class<?>>();

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
