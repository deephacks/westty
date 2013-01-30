package org.deephacks.westty.job;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.quartz.SchedulerConfigException;
import org.quartz.spi.ThreadPool;

@Singleton
public class JobThreadPool implements ThreadPool {

    private final ThreadPoolExecutor executor;

    @Inject
    public JobThreadPool(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    @Override
    public boolean runInThread(Runnable runnable) {
        try {
            executor.execute(runnable);
            return true;
        } catch (RejectedExecutionException e) {
            return false;
        }

    }

    @Override
    public int blockForAvailableThreads() {
        return executor.getCorePoolSize() - executor.getActiveCount();
    }

    @Override
    public void initialize() throws SchedulerConfigException {

    }

    @Override
    public void shutdown(boolean waitForJobsToComplete) {

    }

    @Override
    public int getPoolSize() {
        return executor.getPoolSize();
    }

    @Override
    public void setInstanceId(String schedInstId) {

    }

    @Override
    public void setInstanceName(String schedName) {

    }

}
