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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.deephacks.tools4j.config.RuntimeContext;
import org.deephacks.tools4j.config.model.Lookup;
import org.deephacks.westty.internal.core.extension.WesttyConfigBootstrap;
import org.deephacks.westty.internal.core.extension.WesttySockJsBootstrap;
import org.deephacks.westty.internal.core.http.WesttyHttpPipelineFactory;
import org.deephacks.westty.persistence.Transactional;
import org.deephacks.westty.spi.WesttyIoExecutors;
import org.deephacks.westty.spi.WesttyModule;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.impl.DefaultVertx;
import org.vertx.java.core.impl.VertxInternal;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSServer;
import org.vertx.java.deploy.Container;
import org.vertx.java.deploy.impl.VerticleManager;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

/**
 * Main class for starting and stopping Westty.
 */
public class WesttyCore {
    private static final Logger log = LoggerFactory.getLogger(WesttyCore.class);
    private static final RuntimeContext ctx = Lookup.get().lookup(RuntimeContext.class);
    private WeldContainer container;
    private Starter engine;

    public void startup() {
        log.info("Westty startup.");
        try {
            Stopwatch time = new Stopwatch().start();
            container = new Weld().initialize();
            log.info("Weld started.");
            engine = container.instance().select(Starter.class).get();
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

    public static class Starter {
        @Inject
        private WesttyEngine engine;

        public Starter() {

        }

        public void start() {
            engine.registerConfig();
            engine.start();
        }

        public void stop() {
            engine.stop();
        }
    }

    @Singleton
    private static class WesttyEngine {
        private static final RuntimeContext ctx = Lookup.get().lookup(RuntimeContext.class);
        @Inject
        private WesttyHttpPipelineFactory coreFactory;
        @Inject
        private WesttyConfigBootstrap configBootstrap;
        @Inject
        private WesttySockJsBootstrap sockJsBootstrap;
        @Inject
        private WesttyIoExecutors executors;
        @Inject
        private Instance<WesttyModule> modules;
		
        private VertxInternal vertx = new DefaultVertx();
		
        private VerticleManager mgr = new VerticleManager(vertx);
        
        private HttpServer server = vertx.createHttpServer();
        
        @Inject
        private EventBus bus;

        private Channel standardChannel;
        private ServerBootstrap standardBootstrap;

        public WesttyEngine() {

        }
        
        @Produces
        @Singleton
        public EventBus createEventBus() {
            return vertx.eventBus();
        }
        @Transactional
        public void registerConfig() {
            ctx.register(configBootstrap.getSchemas());
            ctx.registerDefault(configBootstrap.getDefaults());
        }

        @Transactional
        public void start() {

            for (WesttyModule module : sortModules()) {
                log.info("Starting WesttyModule {}", module.getClass().getName());
                module.startup();
                log.info("WesttyModule ready {}", module.getClass().getName());
            }
            startInternal();
        }

        public void startInternal() {
            startHttp();
            startSockJs();
        }

        private ArrayList<WesttyModule> sortModules() {
            ArrayList<WesttyModule> result = Lists.newArrayList(modules);
            Collections.sort(result, new Comparator<WesttyModule>() {

                @Override
                public int compare(WesttyModule m1, WesttyModule m2) {
                    if (m1.priority() < m2.priority()) {
                        return -1;
                    }
                    return 1;
                }
            });
            return result;
        }

        private void startHttp() {
            int ioWorkerCount = 4;
            int port = 8080;
            standardBootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
                    executors.getBoss(), executors.getWorker(), ioWorkerCount));
            standardBootstrap.setPipelineFactory(coreFactory);
            standardChannel = standardBootstrap.bind(new InetSocketAddress(port));

            log.info("Http listening on port {}.", port);
        }
        
        private void startSockJs() {
    	    JsonArray permitted = new JsonArray();
    	    // Let everything through
    	    permitted.add(new JsonObject()); 
            SockJSServer sockJSServer = vertx.createSockJSServer(server);
    	    sockJSServer.bridge(new JsonObject().putString("prefix", "/eventbus"), permitted, permitted);
    	    sockJsBootstrap.start(bus);
    	    server.listen(8090);
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
            if(server != null){
            	server.close();
            }
            log.debug("All channels closed.");
        }
    }
}
