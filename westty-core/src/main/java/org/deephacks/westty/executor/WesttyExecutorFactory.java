package org.deephacks.westty.executor;

import java.util.concurrent.TimeUnit;

import org.deephacks.westty.config.ExecutorConfig;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

public class WesttyExecutorFactory {
    private static WesttyExecutor EXECUTOR;

    public static WesttyExecutor create(ExecutorConfig config) {
        if (EXECUTOR != null) {
            return EXECUTOR;
        }
        OrderedMemoryAwareThreadPoolExecutor pool = new OrderedMemoryAwareThreadPoolExecutor(
                config.getCorePoolSize(), config.getMaxChannelMemorySize(),
                config.getMaxTotalMemorySize(), config.getKeepAliveTime(), TimeUnit.SECONDS);
        EXECUTOR = new WesttyExecutor(pool);
        return EXECUTOR;
    }
}
