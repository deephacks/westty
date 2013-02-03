package org.deephacks.westty.internal.job;

import javax.inject.Inject;

import org.deephacks.westty.spi.WesttyModule;
import org.quartz.SchedulerException;

class JobModule implements WesttyModule {
    @Inject
    private JobScheduler scheduler;

    @Override
    public void startup() {
        try {
            scheduler.start();
            scheduler.schedule();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void shutdown() {
        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int priority() {
        return 5000;
    }

}
