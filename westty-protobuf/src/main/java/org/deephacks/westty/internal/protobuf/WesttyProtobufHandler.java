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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.nio.channels.ClosedChannelException;
import java.util.HashMap;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.deephacks.westty.protobuf.FailureMessages.Failure;
import org.deephacks.westty.protobuf.ProtobufException;
import org.deephacks.westty.protobuf.ProtobufException.FailureCode;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

class WesttyProtobufHandler extends SimpleChannelHandler {
    private static final Logger log = LoggerFactory.getLogger(WesttyProtobufHandler.class);
    @Inject
    private WesttyProtobufExtension extension;
    private HashMap<Class<?>, Method> endpoints;
    private BeanManager beanManager;

    @Override
    public void handleUpstream(final ChannelHandlerContext ctx, final ChannelEvent e)
            throws Exception {
        if (e instanceof ChannelStateEvent) {
            log.debug(e.toString());
        }
        super.handleUpstream(ctx, e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        Object res = invokeEndpoint(e.getMessage());
        if (res != null && e.getChannel().isConnected()) {
            e.getChannel().write(res);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        final Throwable cause = e.getCause();
        final Channel ch = ctx.getChannel();
        if (cause instanceof ClosedChannelException) {
            log.warn("Attempt to write to closed channel " + ch);
        } else if (cause instanceof IOException
                && "Connection reset by peer".equals(cause.getMessage())) {
            // a client may have disconnected
        } else if (cause instanceof ConnectException
                && "Connection refused".equals(cause.getMessage())) {
            // server not up, nothing to do 
        } else {
            log.error("Unexpected exception downstream for " + ch, cause);
            e.getChannel().close();
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object invokeEndpoint(Object proto) {
        if (endpoints == null) {
            endpoints = extension.getEndpoints();
            beanManager = extension.getBeanManager();
        }
        Class<?> cls = proto.getClass();
        Method method = endpoints.get(cls);

        Set<Bean<?>> protoBeans = beanManager.getBeans(method.getDeclaringClass());

        Bean protoBean = beanManager.resolve(protoBeans);
        CreationalContext cc = beanManager.createCreationalContext(protoBean);
        Object endpoint = beanManager.getReference(protoBean, Object.class, cc);
        Object res = null;
        try {
            res = method.invoke(endpoint, proto);
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
        } finally {
            protoBean.destroy(endpoint, cc);
        }
        return res;
    }
}
