package org.deephacks.westty.internal.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShutdownHook {
    private static final Logger log = LoggerFactory.getLogger(ShutdownHook.class);

    static void install(final Thread threadToJoin) {
        Thread thread = new ShutdownHookThread(threadToJoin);
        Runtime.getRuntime().addShutdownHook(thread);
        log.debug("Create shutdownhook: " + thread.getName());
    }

    private static class ShutdownHookThread extends Thread {
        private final Thread threadToJoin;

        private ShutdownHookThread(final Thread threadToJoin) {
            super("ShutdownHook: " + threadToJoin.getName());
            this.threadToJoin = threadToJoin;
        }

        @Override
        public void run() {
            log.debug("Starting " + getName());
            shutdown(threadToJoin, 30000);
            log.debug("Finished " + getName());
        }
    }

    public static void shutdown(final Thread t, final long joinwait) {
        if (t == null)
            return;
        t.start();
        while (t.isAlive()) {
            try {
                t.join(joinwait);
            } catch (InterruptedException e) {
                log.warn(t.getName() + "; joinwait=" + joinwait, e);
            }
        }
    }

}
