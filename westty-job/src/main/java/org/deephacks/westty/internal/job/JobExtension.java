package org.deephacks.westty.internal.job;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.deephacks.westty.job.Job;
import org.deephacks.westty.job.Schedule;

public class JobExtension implements Extension {
    private static BeanManager bm;
    private static final Set<Class<? extends Job>> jobs = new HashSet<Class<? extends Job>>();

    public void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager bm) {
        JobExtension.bm = bm;
    }

    void processAnnotatedType(@Observes ProcessAnnotatedType<?> pat) {
        AnnotatedType<?> t = pat.getAnnotatedType();
        Schedule schedule = t.getAnnotation(Schedule.class);
        if (schedule == null) {
            return;
        }
        Class<? extends Job> cls = t.getJavaClass().asSubclass(Job.class);
        jobs.add(cls);
    }

    public static Set<Class<? extends Job>> getJobs() {
        return jobs;
    }

    public static BeanManager getBeanManager() {
        return bm;
    }
}
