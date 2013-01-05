package org.deephacks.westty.job;

import javax.enterprise.inject.Instance;

import org.deephacks.tools4j.config.RuntimeContext;
import org.deephacks.tools4j.config.model.Lookup;
import org.deephacks.westty.config.JpaConfig;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class JobTest {

    static {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.WARN);
        RuntimeContext ctx = Lookup.get().lookup(RuntimeContext.class);
        ctx.register(JpaConfig.class);
        ctx.register(JobSchedulerConfig.class);
        ctx.register(JobConfig.class);
        JpaConfig jpa = ctx.singleton(JpaConfig.class);
        jpa.dropInstall();

    }
    private static final WeldContainer container = new Weld().initialize();
    private static final Instance<JobScheduler> instance = container.instance().select(
            JobScheduler.class);

    public static void main(String[] args) throws Exception {

    }

}
