package org.deephacks.westty.internal.job;

import org.quartz.spi.ThreadExecutor;

class JobExecutor implements ThreadExecutor {

    @Override
    public void execute(Thread thread) {
        thread.start();
    }

    @Override
    public void initialize() {

    }

}
