package org.deephacks.westty.spi;

public interface WesttyModule {

    public void startup();

    public void shutdown();

    /**
     * Values are treated like a prioritized list of tasks;
     * where 1 is top priority and higher values follows.
     */
    public int priority();
}
