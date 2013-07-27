/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.deephacks.westty.internal.core;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import org.deephacks.confit.ConfigContext;
import org.deephacks.westty.application.ApplicationShutdownEvent;
import org.deephacks.westty.application.ApplicationStartupEvent;
import org.deephacks.westty.config.ServerConfig;
import org.deephacks.westty.server.ServerName;
import org.deephacks.westty.spi.ProviderShutdownEvent;
import org.deephacks.westty.spi.ProviderStartupEvent;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;

/**
 * Main class for starting and stopping Westty.
 */
public class WesttyCore {
    public static final String APPLICATION_CONF_PROP = "application.conf";
    public static final String APPLICATION_CONF = System.getProperty(APPLICATION_CONF_PROP);
    private static final Logger log = LoggerFactory.getLogger(WesttyCore.class);
    private WeldContainer container;
    private Weld weld;
    private WesttyEngine engine;
    private static ServerName SERVER_NAME = new ServerName(ServerConfig.DEFAULT_SERVER_NAME);

    public void startup() {
        log.info("Westty startup.");
        try {
            Stopwatch time = new Stopwatch().start();
            if (!Strings.isNullOrEmpty(APPLICATION_CONF)) {
                File conf = new File(APPLICATION_CONF);
                if (conf.exists()) {
                    System.setProperty(APPLICATION_CONF_PROP, conf.getAbsolutePath());
                }
            }
            weld = new Weld();
            container = weld.initialize();
            log.info("Weld started.");

            engine = container.instance().select(WesttyEngine.class).get();
            engine.start();
            ShutdownHook.install(new Thread("WesttyShutdownHook") {
                @Override
                public void run() {
                    shutdown();
                }
            });
            log.info("Westty started in {} ms.", time.elapsedMillis());
        } catch (Exception e) {
            log.error("Exception during startup", e);
            shutdown();
            throw new RuntimeException(e);
        }
    }

    public void setServerName(String name){
        SERVER_NAME = new ServerName(name);
    }

    @Produces
    @Singleton
    public ServerName produceServerName(){
        return SERVER_NAME;
    }

    public void shutdown() {
        if (engine != null) {
            engine.shutdown();
        }
        log.info("Westty shutdown.");
    }

    public Object getInstance(Class<?> cls) {
        return container.instance().select(cls).get();
    }

    @Singleton
    private static class WesttyEngine {
        @Inject
        private ConfigContext config;

        @Inject
        private Event<ApplicationStartupEvent> applicationStartupEvent;

        @Inject
        private Event<ApplicationShutdownEvent> applicationShutdownEvent;

        @Inject
        private Event<ProviderStartupEvent> providerStartupEvent;

        @Inject
        private Event<ProviderShutdownEvent> providerShutdownEvent;

        public void start() {
            providerStartupEvent.fire(new ProviderStartupEvent());
            applicationStartupEvent.fire(new ApplicationStartupEvent());
        }

        public void shutdown() {
            applicationShutdownEvent.fire(new ApplicationShutdownEvent());
            providerShutdownEvent.fire(new ProviderShutdownEvent());
        }
    }
}
