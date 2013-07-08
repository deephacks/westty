package org.deephacks.westty.internal.sockjs;

import org.deephacks.westty.sockjs.SockJsEndpoint;
import org.deephacks.westty.sockjs.SockJsMessage;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.inject.Singleton;

@Singleton
public class SockJsEnpointExtension implements Extension {
    private SockJsEndpoints endpoints;

    public <X> void processAnnotatedType(@Observes ProcessAnnotatedType<X> pat, BeanManager beanManager) {
        if(endpoints == null){
            endpoints = new SockJsEndpoints(beanManager);
        }
        if (!pat.getAnnotatedType().isAnnotationPresent(SockJsEndpoint.class)) {
            return;
        }
        final AnnotatedType<X> org = pat.getAnnotatedType();

        for (AnnotatedMethod<?> method : org.getMethods()) {
            SockJsMessage anno = method.getAnnotation(SockJsMessage.class);
            if (anno == null) {
                continue;
            }
            endpoints.put(anno.value(), method.getJavaMember());
        }
    }

    public SockJsEndpoints getEndpoints(){
        return endpoints;
    }
}
