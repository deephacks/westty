package org.deephacks.westty.spi;

import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
public class IoExecutors {

    private ExecutorService bossExecutor = Executors.newCachedThreadPool();
    private ExecutorService ioExecutor = Executors.newCachedThreadPool();

    public ExecutorService getBoss() {
        return bossExecutor;
    }

    public ExecutorService getWorker() {
        return ioExecutor;
    }

}
