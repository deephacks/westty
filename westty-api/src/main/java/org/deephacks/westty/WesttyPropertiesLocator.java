package org.deephacks.westty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Lists;

@Singleton
public class WesttyPropertiesLocator {
    @Inject
    private Instance<WesttyPropertyExtension> factories;
    private WesttyProperties properties;

    @Produces
    public WesttyProperties produce() {

        if (properties != null) {
            return properties;
        }
        ArrayList<WesttyPropertyExtension> list = sort();
        WesttyProperties prop = WesttyProperties.create();
        for (WesttyPropertyExtension factory : list) {
            factory.extendProperties(prop);
        }
        return prop;
    }

    private ArrayList<WesttyPropertyExtension> sort() {
        ArrayList<WesttyPropertyExtension> result = Lists.newArrayList(factories);
        Collections.sort(result, new Comparator<WesttyPropertyExtension>() {

            @Override
            public int compare(WesttyPropertyExtension o1, WesttyPropertyExtension o2) {
                if (o1.priority() < o2.priority()) {
                    return -1;
                }
                return 1;
            }
        });
        return result;
    }
}
