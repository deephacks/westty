package org.deephacks.westty.jaxrs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deephacks.tools4j.config.model.Bean;
import org.deephacks.tools4j.config.model.Bean.BeanId;

public class JaxrsConfigBeans {
    private Collection<JaxrsConfigBean> beans = new ArrayList<JaxrsConfigBean>();
    private long totalCount;

    public void setBeans(Collection<JaxrsConfigBean> beans) {
        this.beans = beans;
    }

    public void addBean(Bean bean) {
        this.beans.add(new JaxrsConfigBean(bean));
    }

    public void setTotalCount(long total) {
        this.totalCount = total;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public Collection<JaxrsConfigBean> getBeans() {
        return beans;
    }

    public static class JaxrsConfigBean {
        private String id;
        private String schemaName;
        private Map<String, List<String>> properties = new HashMap<String, List<String>>();

        public JaxrsConfigBean() {

        }

        public JaxrsConfigBean(Bean bean) {
            this.schemaName = bean.getId().getSchemaName();
            this.id = bean.getId().getInstanceId();
            for (String name : bean.getPropertyNames()) {
                List<String> values = bean.getValues(name);
                if (values == null || values.isEmpty()) {
                    continue;
                }
                properties.put(name, values);
            }
            for (String name : bean.getReferenceNames()) {
                List<BeanId> refs = bean.getReferences();
                if (refs == null || refs.isEmpty()) {
                    continue;
                }
                List<String> values = new ArrayList<String>();
                for (BeanId beanId : refs) {
                    values.add(beanId.getInstanceId());
                }
                properties.put(name, values);
            }
        }

        public String getSchemaName() {
            return schemaName;
        }

        public void setSchemaName(String schemaName) {
            this.schemaName = schemaName;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Map<String, List<String>> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, List<String>> properties) {
            this.properties = properties;
        }

    }

}
