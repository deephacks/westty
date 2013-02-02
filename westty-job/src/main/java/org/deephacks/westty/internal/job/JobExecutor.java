package org.deephacks.westty.internal.job;

import org.quartz.spi.ThreadExecutor;

public class JobExecutor implements ThreadExecutor {

    @Override
    public void execute(Thread thread) {
        thread.start();
    }

    @Override
    public void initialize() {

    }

}
