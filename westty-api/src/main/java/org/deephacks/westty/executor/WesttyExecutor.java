package org.deephacks.westty.executor;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.inject.Alternative;

@Executor
@Alternative
public class WesttyExecutor implements java.util.concurrent.Executor {
    private ThreadPoolExecutor executor;

    public WesttyExecutor() {

    }

    WesttyExecutor(ThreadPoolExecutor executor) {
        executor.setThreadFactory(new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r);
            }
        });
        this.executor = executor;
    }

    public <T> Future<T> submit(Runnable task, T result) {
        return executor.submit(task, result);
    }

    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException,
            ExecutionException {
        return executor.invokeAny(tasks);
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return executor.invokeAny(tasks, timeout, unit);
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
            throws InterruptedException {
        return executor.invokeAll(tasks);
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout,
            TimeUnit unit) throws InterruptedException {
        return executor.invokeAll(tasks, timeout, unit);
    }

    public void execute(Runnable command) {
        executor.execute(command);
    }

    public boolean isShutdown() {
        return executor.isShutdown();
    }

    public boolean isTerminating() {
        return executor.isTerminating();
    }

    public boolean isTerminated() {
        return executor.isTerminated();
    }

    public int getCorePoolSize() {
        return executor.getCorePoolSize();
    }

    public BlockingQueue<Runnable> getQueue() {
        return executor.getQueue();
    }

    public int getPoolSize() {
        return executor.getPoolSize();
    }

    public int getActiveCount() {
        return executor.getActiveCount();
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
