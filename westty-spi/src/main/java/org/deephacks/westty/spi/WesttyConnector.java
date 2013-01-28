package org.deephacks.westty.spi;

import java.util.Properties;
import java.util.concurrent.ExecutorService;

public interface WesttyConnector {

    public Object startup(ExecutorService bossExecutor, ExecutorService ioExecutor, Properties props);

    public void shutdown();

    public int getLoadOrder();
}
