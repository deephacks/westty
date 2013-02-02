package org.deephacks.westty.internal.core.executor;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.deephacks.westty.config.ExecutorConfig;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

@Singleton
public class WesttyExecutor extends OrderedMemoryAwareThreadPoolExecutor {

    @Inject
    public WesttyExecutor(ExecutorConfig config) {
        super(10, 100000, 100000, 100, TimeUnit.SECONDS, new WesttyThreadFactory());
        //        super(config.getCorePoolSize(), config.getMaxChannelMemorySize(), config
        //                .getMaxTotalMemorySize(), config.getKeepAliveTime(), TimeUnit.SECONDS,
        //                new WesttyThreadFactory());

    }

    public <T> Future<T> submit(Runnable task, T result) {
        return super.submit(task, result);
    }

    public <T> Future<T> submit(Callable<T> task) {
        return super.submit(task);
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException,
            ExecutionException {
        return super.invokeAny(tasks);
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return super.invokeAny(tasks, timeout, unit);
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
            throws InterruptedException {
        return super.invokeAll(tasks);
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout,
            TimeUnit unit) throws InterruptedException {
        return super.invokeAll(tasks, timeout, unit);
    }

    public void execute(Runnable command) {
        super.execute(command);
    }

    public boolean isShutdown() {
        return super.isShutdown();
    }

    public boolean isTerminating() {
        return super.isTerminating();
    }

    public boolean isTerminated() {
        return super.isTerminated();
    }

    public int getCorePoolSize() {
        return super.getCorePoolSize();
    }

    public BlockingQueue<Runnable> getQueue() {
        return super.getQueue();
    }

    public int getPoolSize() {
        return super.getPoolSize();
    }

    public int getActiveCount() {
        return super.getActiveCount();
    }

    static class WesttyThreadFactory implements ThreadFactory {
        static final AtomicInteger poolNumber = new AtomicInteger(1);
        final ThreadGroup group;
        final AtomicInteger threadNumber = new AtomicInteger(1);
        final String namePrefix;

        WesttyThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "westty-pool-" + poolNumber.getAndIncrement() + "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

}
