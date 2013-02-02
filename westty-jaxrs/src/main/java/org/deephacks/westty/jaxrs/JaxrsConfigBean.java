package org.deephacks.westty.jaxrs;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.VisibilityChecker;

public class JaxrsConfigBean {
    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(
                JsonAutoDetect.Visibility.ANY));
    }
    private String className;
    private Object bean;

    public JaxrsConfigBean() {

    }

    public JaxrsConfigBean(String className, Object bean) {
        this.className = className;
        this.bean = bean;

    }

    public JaxrsConfigBean(Object o) {
        this.className = o.getClass().getCanonicalName();
        this.bean = o;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public Object getBean() {
        try {
            Class<?> clazz = Class.forName(className);
            return mapper.convertValue(bean, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

}