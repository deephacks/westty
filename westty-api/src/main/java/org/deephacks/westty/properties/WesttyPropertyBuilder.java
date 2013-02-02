package org.deephacks.westty.properties;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({ METHOD })
@Documented
public @interface WesttyPropertyBuilder {
    /**
     * Values are treated like a prioritized list of tasks;
     * where 1 is top priority and higher values follows.
     */
    public int priority() default Integer.MAX_VALUE;

}
