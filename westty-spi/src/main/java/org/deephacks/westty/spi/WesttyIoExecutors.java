package org.deephacks.westty.spi;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

@Singleton
public class WesttyIoExecutors {

    private ExecutorService bossExecutor = Executors.newCachedThreadPool();
    private ExecutorService ioExecutor = Executors.newCachedThreadPool();

    public ExecutorService getBoss() {
        return bossExecutor;
    }

    public ExecutorService getWorker() {
        return ioExecutor;
    }

}
