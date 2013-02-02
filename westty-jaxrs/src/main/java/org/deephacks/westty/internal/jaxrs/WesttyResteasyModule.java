package org.deephacks.westty.internal.jaxrs;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.codehaus.jackson.map.DeserializationConfig;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJacksonProvider;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class WesttyResteasyModule {
    private static final Logger log = LoggerFactory.getLogger(WesttyResteasyModule.class);
    private final ResteasyDeployment deployment;

    @Inject
    public WesttyResteasyModule(ResteasyDeployment deployment, WesttyJaxrsApplication jaxrsApps) {
        this.deployment = deployment;
        deployment.setApplication(jaxrsApps);
        ResteasyJacksonProvider provider = new ResteasyJacksonProvider();
        provider.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ResteasyProviderFactory resteasyFactory = ResteasyProviderFactory.getInstance();
        resteasyFactory.registerProviderInstance(provider);

        deployment.setProviderFactory(resteasyFactory);
        deployment.start();
        log.info("RestEasy started.");
    }

    public void stop() {
        deployment.stop();
    }

    @Produces
    @Singleton
    public ResteasyDeployment createResteasyDeployment() {
        return new ResteasyDeployment();
    }
}
