package org.deephacks.westty.spi;

import java.util.Properties;

public interface WesttyModule {
    public void startup(Properties props);

    public void shutdown();

    public int getLoadOrder();
}
