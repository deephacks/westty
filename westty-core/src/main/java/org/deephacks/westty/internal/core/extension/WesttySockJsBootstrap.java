package org.deephacks.westty.internal.core.extension;

import java.lang.reflect.Method;
import java.util.HashMap;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.inject.Singleton;

import org.deephacks.westty.internal.core.extension.WesttyCoreExtensionImpl.WesttyCoreExtension;
import org.deephacks.westty.sockjs.SockJsEndpoint;
import org.deephacks.westty.sockjs.SockJsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;

@Singleton
public class WesttySockJsBootstrap extends WesttyCoreExtension {
    private static final Logger log = LoggerFactory.getLogger(WesttySockJsBootstrap.class);
    private static final HashMap<String, Method> sockjsToEndpoint = new HashMap<String, Method>();
    
    public <X> void processAnnotatedType(@Observes ProcessAnnotatedType<X> pat) {
        if (!pat.getAnnotatedType().isAnnotationPresent(SockJsEndpoint.class)) {
            return;
        }
        final AnnotatedType<X> org = pat.getAnnotatedType();

        for (AnnotatedMethod<?> method : org.getMethods()) {
        	SockJsMessage anno = method.getAnnotation(SockJsMessage.class);
            if (anno == null) {
                continue;
            }
           	sockjsToEndpoint.put(anno.value(), method.getJavaMember());
        }
    }
	public void start(EventBus bus) {
    	for (String address : sockjsToEndpoint.keySet()) {
			try {
				final Method m = sockjsToEndpoint.get(address);
				Class<?> cls = m.getDeclaringClass();
				final Object o = cls.newInstance();
	    		bus.registerHandler(address, new Handler<Message<?>>() {
					@Override
					public void handle(Message<?> event) {
						try {
							m.invoke(o, event);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				});
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}
	}
    


}
