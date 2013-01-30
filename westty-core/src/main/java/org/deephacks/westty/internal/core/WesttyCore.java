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
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.codehaus.jackson.map.DeserializationConfig;
import org.deephacks.tools4j.config.RuntimeContext;
import org.deephacks.tools4j.config.model.Lookup;
import org.deephacks.westty.config.ServerConfig;
import org.deephacks.westty.internal.core.ssl.WesttySecurePipelineFactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJacksonProvider;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

/**
 * Main class for starting and stopping Westty.
 */
public class WesttyCore {
    private static final Logger log = LoggerFactory.getLogger(WesttyCore.class);
    private static final RuntimeContext ctx = Lookup.get().lookup(RuntimeContext.class);
    private WeldContainer container;
    private WesttyEngine engine;
    private File rootDir;
    private Properties properties = new Properties();

    public WesttyCore() {
    }

    public void setRootDir(File dir) {
        this.rootDir = dir;
    }

    public void setProperties(Properties props) {
        for (String key : props.stringPropertyNames()) {
            properties.setProperty(key, props.getProperty(key));
        }
    }

    public void startup() {

        log.info("Westty startup.");
        Callable<Object> start = new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                Stopwatch time = new Stopwatch().start();
                container = new Weld().initialize();
                log.info("Weld started.");
                engine = container.instance().select(WesttyEngine.class).get();
                engine.setConfig(ctx.singleton(ServerConfig.class));
                engine.start();
                ShutdownHook.install(new Thread("WesttyCore") {
                    @Override
                    public void run() {
                        shutdown();
                    }
                });
                log.info("Westty started in {} ms.", time.elapsedMillis());
                return null;
            }
        };
        /**
         * TODO: Do startup and shutdown using pipleline pattern that 
         * discovers service loaders during bootstrap. This will
         * also decouple westty core from jpa, job and other 
         * future extensions. 
         */
        try {
            start.call();
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

    private static class WesttyEngine {
        private ServerConfig config;
        @Inject
        private WesttyPipelineFactory coreFactory;
        @Inject
        private WesttySecurePipelineFactory secureFactory;
        @Inject
        private WesttyJaxrsApplication jaxrsApps;
        @Inject
        private ResteasyDeployment deployment;
        private Channel standardChannel;
        private Channel secureChannel;
        private ServerBootstrap standardBootstrap;
        private ServerBootstrap secureBootstrap;

        public void setConfig(ServerConfig config) {
            this.config = config;

        }

        public void start() {
            startRestEasy();
            startHttp();
            startHttps();
        }

        private void startRestEasy() {
            deployment.setApplication(jaxrsApps);
            ResteasyJacksonProvider provider = new ResteasyJacksonProvider();
            provider.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            ResteasyProviderFactory resteasyFactory = ResteasyProviderFactory.getInstance();
            resteasyFactory.registerProviderInstance(provider);

            deployment.setProviderFactory(resteasyFactory);
            deployment.start();
            log.info("RestEasy started.");
        }

        private void startHttp() {
            int port = config.getHttpPort();
            standardBootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
                    Executors.newCachedThreadPool(), Executors.newCachedThreadPool(),
                    config.getIoWorkerCount()));
            coreFactory.setConfig(config);
            standardBootstrap.setPipelineFactory(coreFactory);
            standardChannel = standardBootstrap.bind(new InetSocketAddress(port));

            log.info("Http listening on port {}.", port);
        }

        private void startHttps() {
            int port = config.getHttpsPort();
            if (!config.getSsl().getSslEnabled()) {
                return;
            }

            secureBootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
                    Executors.newCachedThreadPool(), Executors.newCachedThreadPool(),
                    config.getIoWorkerCount()));
            secureBootstrap.setPipelineFactory(secureFactory);
            secureChannel = secureBootstrap.bind(new InetSocketAddress(port));

            log.info("Https listening on port {}.", port);
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
            if (secureChannel != null) {
                standardChannel.close().awaitUninterruptibly(5000);
            }
            if (secureBootstrap != null) {
                standardBootstrap.releaseExternalResources();
            }

            if (deployment != null) {
                deployment.stop();
            }

            log.debug("All channels closed.");
        }

        /**
         * ResteasyDeployment is not injectable. 
         */
        @SuppressWarnings("unused")
        @Produces
        @ApplicationScoped
        public ResteasyDeployment createResteasyDeployment() {
            return new ResteasyDeployment();
        }
    }
}
