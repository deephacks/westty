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
package org.deephacks.westty.internal.protobuf;

import java.lang.reflect.Method;
import java.util.HashMap;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.deephacks.westty.protobuf.Protobuf;
import org.deephacks.westty.protobuf.ProtobufMethod;
import org.deephacks.westty.protobuf.ProtobufSerializer;

class WesttyProtobufExtension implements Extension {
    public static final ProtobufSerializer serializer = new ProtobufSerializer();
    private HashMap<Class<?>, Method> protoToEndpoint = new HashMap<Class<?>, Method>();
    private BeanManager beanManager;

    <X> void processAnnotatedType(@Observes ProcessAnnotatedType<X> pat) {

        if (!pat.getAnnotatedType().isAnnotationPresent(Protobuf.class)) {
            return;
        }
        final AnnotatedType<X> org = pat.getAnnotatedType();

        Protobuf proto = org.getAnnotation(Protobuf.class);
        for (String protodesc : proto.value()) {
            String resource = "META-INF/" + protodesc + ".desc";
            serializer.registerResource(resource);
        }

        for (AnnotatedMethod<?> method : org.getMethods()) {
            ProtobufMethod anno = method.getAnnotation(ProtobufMethod.class);
            if (anno == null) {
                continue;
            }
            for (Class<?> param : method.getJavaMember().getParameterTypes()) {
                protoToEndpoint.put(param, method.getJavaMember());
            }
        }
    }

    public void start(@Observes AfterDeploymentValidation event, BeanManager bm) {
        beanManager = bm;
    }

    public BeanManager getBeanManager() {
        return beanManager;
    }

    public HashMap<Class<?>, Method> getEndpoints() {
        return protoToEndpoint;
    }

    public ProtobufSerializer getSerializer() {
        return serializer;
    }

}
