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

import java.io.File;
import java.net.InetSocketAddress;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.deephacks.tools4j.config.RuntimeContext;
import org.deephacks.westty.config.ServerConfig;
import org.deephacks.westty.internal.core.extension.WesttyConfigBootstrap;
import org.deephacks.westty.internal.core.http.WesttyHttpPipelineFactory;
import org.deephacks.westty.properties.WesttyProperties;
import org.deephacks.westty.spi.WesttyIoExecutors;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;

/**
 * Main class for starting and stopping Westty.
 */
public class WesttyCore {
    public static final String WESTTY_ROOT_PROP = "westty.root.dir";
    public static final String WESTTY_ROOT = System.getProperty(WESTTY_ROOT_PROP);

    private static final Logger log = LoggerFactory.getLogger(WesttyCore.class);
    private WeldContainer container;
    private WesttyEngine engine;

    public void startup() {
        log.info("Westty startup.");
        try {
            Stopwatch time = new Stopwatch().start();
            if (!Strings.isNullOrEmpty(WESTTY_ROOT)) {
                File root = new File(WESTTY_ROOT);
                if (root.exists()) {
                    WesttyProperties.init(root);
                }
            }
            container = new Weld().initialize();
            log.info("Weld started.");

            engine = container.instance().select(WesttyEngine.class).get();
            engine.start();
            ShutdownHook.install(new Thread("WesttyCore") {
                @Override
                public void run() {
                    shutdown();
                }
            });
            log.info("Westty started in {} ms.", time.elapsedMillis());
        } catch (Exception e) {
            e.printStackTrace();
            shutdown();
        }

    }

    public void shutdown() {
        if (engine != null) {
            engine.stop();
        }
        log.info("Westty shutdown.");
    }

    public Object getInstance(Class<?> cls) {
        return container.instance().select(cls).get();
    }

    @Singleton
    private static class WesttyEngine {
        @Inject
        private RuntimeContext ctx;

        @Inject
        private WesttyHttpPipelineFactory coreFactory;

        @Inject
        private WesttyConfigBootstrap configBootstrap;

        @Inject
        private WesttyIoExecutors executors;

        private Channel standardChannel;
        private ServerBootstrap standardBootstrap;

        public void start() {
            ctx.register(configBootstrap.getSchemas());
            ctx.registerDefault(configBootstrap.getDefaults());
            startHttp();
        }

        private void startHttp() {
            ServerConfig config = ctx.singleton(ServerConfig.class);
            int ioWorkerCount = config.getIoWorkerCount();
            int port = config.getHttpPort();
            standardBootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
                    executors.getBoss(), executors.getWorker(), ioWorkerCount));
            standardBootstrap.setPipelineFactory(coreFactory);
            standardChannel = standardBootstrap.bind(new InetSocketAddress(port));

            log.info("Http listening on port {}.", port);
        }

        public void stop() {
            log.debug("Closing channels.");
            if (coreFactory != null) {
                coreFactory.close();
            }
            if (standardChannel != null) {
                standardChannel.close().awaitUninterruptibly(5000);
            }
            if (standardBootstrap != null) {
                standardBootstrap.releaseExternalResources();
            }

            log.debug("All channels closed.");
        }
    }
}
