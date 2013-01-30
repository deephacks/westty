package org.deephacks.westty.spi;

import java.util.Properties;

public interface WesttyModule {
    public void startup();

    public void shutdown();

    public int getLoadOrder();
}
