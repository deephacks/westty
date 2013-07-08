package org.deephacks.westty.internal.sockjs;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;

@Alternative
public class SockJsEndpoints {
    private static final HashMap<String, Method> sockjsToEndpoint = new HashMap<>();
    private BeanManager beanManager;

    public SockJsEndpoints(BeanManager beanManager) {
        this.beanManager = beanManager;
    }

    public void put(String address, Method method) {
        sockjsToEndpoint.put(address, method);
    }

    public void start(EventBus bus) {
        for (String address : sockjsToEndpoint.keySet()) {
            try {
                final Method method = sockjsToEndpoint.get(address);
                Class<?> methodDeclaringClass = method.getDeclaringClass();
                Set<Bean<?>> protoBeans = beanManager.getBeans(methodDeclaringClass);
                Bean<?> protoBean = beanManager.resolve(protoBeans);
                CreationalContext<?> cc = beanManager.createCreationalContext(protoBean);
                final Object endpoint = beanManager.getReference(protoBean, Object.class, cc);
                Handler handler = new Handler<Message<?>>() {
                    @Override
                    public void handle(Message<?> event) {
                        try {
                            method.invoke(endpoint, event);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
                bus.registerHandler(address, handler);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
