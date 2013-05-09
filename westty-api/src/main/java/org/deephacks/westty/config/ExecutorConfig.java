package org.deephacks.westty.config;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.deephacks.tools4j.config.Config;
import org.deephacks.tools4j.config.ConfigScope;

@Config(name = "westty.executor",
        desc = "Westty thread pool executor. Changes requires server restart.")
@ConfigScope
public class ExecutorConfig {

    @Config(desc = "The maximum number of active threads.")
    @NotNull
    @Size(min = 1, max = 65535)
    private Integer corePoolSize = 20;

    @Config(desc = "The maximum total size of the queued events in bytes. 0 to disable.")
    @NotNull
    private Long maxChannelMemorySize = 1048560L;

    @Config(desc = "The maximum total size of the queued events. 0 to disable.")
    @NotNull
    private Long maxTotalMemorySize = 16776960L;

    @Config(desc = "The amount of time for an inactive thread to shut itself down in seconds.")
    @NotNull
    private Integer keepAliveTime = 60;

    public Integer getCorePoolSize() {
        return corePoolSize;
    }

    public Long getMaxChannelMemorySize() {
        return maxChannelMemorySize;
    }

    public Long getMaxTotalMemorySize() {
        return maxTotalMemorySize;
    }

    public Integer getKeepAliveTime() {
        return keepAliveTime;
    }

}
