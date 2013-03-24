package org.deephacks.westty.internal.core.extension;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

@SuppressWarnings("unused")
public class WesttyCoreExtensionImpl implements Extension {
    private static final List<WesttyCoreExtension> extensions = new ArrayList<WesttyCoreExtension>();
    static {
        extensions.add(new WesttyPropertiesBootstrap());
        extensions.add(new WesttyConfigBootstrap());
        extensions.add(new WesttySockJsBootstrap());
    }

    public static class WesttyCoreExtension {
        public void afterBeanDiscovery(AfterBeanDiscovery abd, BeanManager bm) {

        }

        public <X> void processAnnotatedType(@Observes ProcessAnnotatedType<X> pat) {

        }

    }

    private void afterBeanDiscoveryImpl(@Observes AfterBeanDiscovery abd, BeanManager bm) {
        for (WesttyCoreExtension extension : extensions) {
            extension.afterBeanDiscovery(abd, bm);
        }
    }

    private <X> void processAnnotatedTypeImpl(@Observes ProcessAnnotatedType<X> pat) {
        for (WesttyCoreExtension extension : extensions) {
            extension.processAnnotatedType(pat);
        }
    }

}
