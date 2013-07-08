package org.deephacks.westty.internal.protobuf;

import com.google.common.base.Strings;
import org.deephacks.westty.protobuf.FailureMessages.Failure;
import org.deephacks.westty.protobuf.ProtobufException;
import org.deephacks.westty.protobuf.ProtobufException.FailureCode;
import org.deephacks.westty.protobuf.VoidMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Alternative
public class ProtobufEndpoints {
    private ConcurrentHashMap<Class<?>, Method> methodEndpoints = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Method, ProtobufEndpointProxy> proxies = new ConcurrentHashMap<>();
    private BeanManager beanManager;

    public ProtobufEndpoints(BeanManager beanManager){
        this.beanManager = beanManager;
    }
    public void put(Class<?> cls, Method method){
        methodEndpoints.put(cls, method);
    }

    public ProtobufEndpointProxy get(Object protoMsg) {
        Class<?> cls = protoMsg.getClass();
        Method method = methodEndpoints.get(cls);
        if (method == null) {
            return null;
        }
        ProtobufEndpointProxy proxy = proxies.get(method);
        if(proxy != null){
            return proxy;
        }

        Class<?> methodDeclaringClass = method.getDeclaringClass();
        Set<Bean<?>> protoBeans = beanManager.getBeans(methodDeclaringClass);
        Bean<?> protoBean = beanManager.resolve(protoBeans);
        CreationalContext<?> cc = beanManager.createCreationalContext(protoBean);
        Object endpoint = beanManager.getReference(protoBean, Object.class, cc);
        proxy = new ProtobufEndpointProxy(endpoint, method);
        proxies.put(method, proxy);
        return proxy;
    }

    public static class ProtobufEndpointProxy {
        private static final Logger log = LoggerFactory.getLogger(ProtobufEndpointProxy.class);
        private Object endpoint;
        private Method method;
        private boolean voidReturnType;

        private ProtobufEndpointProxy(Object endpoint, Method method){
            this.endpoint = endpoint;
            this.method = method;
            Class<?> returnType = method.getReturnType();
            voidReturnType = returnType.equals(Void.TYPE);
        }

        public Object invoke(Object protoMsg){
            Object res = null;
            try {
                res = method.invoke(endpoint, protoMsg);
            } catch (InvocationTargetException e) {
                Throwable ex = e.getCause();
                log.debug("", ex);
                if (ex instanceof ProtobufException) {
                    ProtobufException pex = (ProtobufException) ex;
                    res = Failure.newBuilder().setCode(pex.getCode()).setMsg(pex.getProtobufMessage())
                            .build();
                } else if (ex instanceof IllegalArgumentException) {
                    res = Failure.newBuilder().setCode(FailureCode.BAD_REQUEST.getCode())
                            .setMsg(ex.getMessage()).build();
                } else if (ex instanceof UnsupportedOperationException) {
                    res = Failure.newBuilder().setCode(FailureCode.NOT_IMPLEMENTED.getCode())
                            .setMsg(ex.getMessage()).build();
                } else if (ex instanceof IllegalStateException) {
                    res = Failure.newBuilder().setCode(FailureCode.CONFLICT.getCode())
                            .setMsg(ex.getMessage()).build();
                } else if (ex instanceof Exception) {
                    String message = Strings.nullToEmpty(ex.getMessage());
                    res = Failure.newBuilder().setCode(FailureCode.INTERNAL_ERROR.getCode())
                            .setMsg(message).build();
                }
            } catch (Exception ex) {
                res = Failure.newBuilder().setCode(FailureCode.INTERNAL_ERROR.getCode())
                        .setMsg(ex.getMessage()).build();
            }
            // if the proxy returns null we must make sure to deliver
            // a response to the client in order to release the callback
            if(res == null && !voidReturnType){
               res = VoidMessage.Void.newBuilder().build();
            }
            return res;
        }
    }
}
