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

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.codehaus.jackson.map.DeserializationConfig;
import org.deephacks.westty.config.ServerConfig;
import org.deephacks.westty.job.JobScheduler;
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
    private WeldContainer container;
    private WesttyEngine engine;

    public WesttyCore() {
    }

    public void startup() {
        Stopwatch time = new Stopwatch().start();
        log.info("Westty startup.");
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
    }

    public void shutdown() {
        engine.stop();
        log.info("Westty shutdown.");
    }

    private static class WesttyEngine {
        @Inject
        private ServerConfig config;
        @Inject
        private WesttyPipelineFactory coreFactory;
        @Inject
        private WesttySecurePipelineFactory secureFactory;
        @Inject
        private WesttyProtobufPipelineFactory protoFactory;
        @Inject
        private WesttyJaxrsApplication jaxrsApps;
        @Inject
        private ResteasyDeployment deployment;
        @Inject
        private JobScheduler scheduler;
        private Channel standardChannel;
        private Channel secureChannel;
        private Channel protoChannel;
        private ServerBootstrap standardBootstrap;
        private ServerBootstrap secureBootstrap;
        private ServerBootstrap protoBootstrap;

        public void start() {
            startRestEasy();
            startHttp();
            startHttps();
            startProtobuf();
        }

        private void startProtobuf() {
            int port = config.getProtobufPort();
            protoBootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
                    Executors.newCachedThreadPool(), Executors.newCachedThreadPool(),
                    config.getIoWorkerCount()));
            protoBootstrap.setPipelineFactory(protoFactory);
            protoBootstrap.setOption("child.tcpNoDelay", true);
            protoBootstrap.setOption("child.keepAlive", true);
            protoChannel = protoBootstrap.bind(new InetSocketAddress(port));
            log.info("Protobuf listening on port {}.", port);
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
            standardChannel.close().awaitUninterruptibly();
            standardBootstrap.releaseExternalResources();
            deployment.stop();
            protoChannel.close().awaitUninterruptibly();
            protoBootstrap.releaseExternalResources();
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
