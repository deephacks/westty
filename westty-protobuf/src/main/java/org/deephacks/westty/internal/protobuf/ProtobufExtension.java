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

import org.deephacks.westty.protobuf.Protobuf;
import org.deephacks.westty.protobuf.ProtobufMethod;
import org.deephacks.westty.protobuf.ProtobufSerializer;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.inject.Singleton;

@Singleton
public class ProtobufExtension implements Extension {
    private ProtobufSerializer serializer = new ProtobufSerializer();
    private ProtobufEndpoints endpoints;

    <X> void processAnnotatedType(@Observes ProcessAnnotatedType<X> pat, BeanManager bm) {
        if(endpoints == null){
            endpoints = new ProtobufEndpoints(bm);
        }
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
                endpoints.put(param, method.getJavaMember());
            }
        }
    }

    public ProtobufEndpoints getEndpoints() {
        return endpoints;
    }

    public ProtobufSerializer getSerializer() {
        return serializer;
    }

}
