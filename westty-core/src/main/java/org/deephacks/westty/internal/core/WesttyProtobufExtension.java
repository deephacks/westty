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

import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.deephacks.westty.protobuf.Protobuf;
import org.deephacks.westty.protobuf.ProtobufMethod;
import org.deephacks.westty.protobuf.ProtobufSerializer;

public class WesttyProtobufExtension implements Extension {

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
            URL url = Thread.currentThread().getContextClassLoader()
                    .getResource("META-INF/" + protodesc + ".desc");
            serializer.register(url);
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

    public ProtobufSerializer getSerializer() {
        return serializer;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object invokeEndpoint(Object proto) {
        Class<?> cls = proto.getClass();
        Method method = protoToEndpoint.get(cls);

        Set<Bean<?>> protoBeans = beanManager.getBeans(method.getDeclaringClass());

        Bean protoBean = beanManager.resolve(protoBeans);
        CreationalContext cc = beanManager.createCreationalContext(protoBean);
        Object endpoint = beanManager.getReference(protoBean, Object.class, cc);
        try {
            return method.invoke(endpoint, proto);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            protoBean.destroy(endpoint, cc);
        }
    }
}
