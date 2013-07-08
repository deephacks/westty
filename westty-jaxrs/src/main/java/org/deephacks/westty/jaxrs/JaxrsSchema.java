package org.deephacks.westty.jaxrs;

import org.deephacks.tools4j.config.model.Schema;

import java.util.ArrayList;
import java.util.List;

public class JaxrsSchema {
    private String schemaName;
    private String className;
    private String desc;
    private String idName;
    private String idDesc;
    private boolean singleton;
    private List<String> propertyNames = new ArrayList<String>();
    private List<SchemaProperty> property = new ArrayList<SchemaProperty>();
    private List<SchemaPropertyList> propertyList = new ArrayList<SchemaPropertyList>();
    private List<SchemaPropertyRef> propertyRef = new ArrayList<SchemaPropertyRef>();
    private List<SchemaPropertyRefList> propertyRefList = new ArrayList<SchemaPropertyRefList>();
    private List<SchemaPropertyRefMap> propertyRefMap = new ArrayList<SchemaPropertyRefMap>();

    public JaxrsSchema(){

    }

    public JaxrsSchema(Schema schema) {
        this.schemaName = schema.getName();
        this.className = schema.getType();
        this.desc = schema.getDesc();
        this.idName = schema.getId().getName();
        this.idDesc = schema.getId().getDesc();
        this.singleton = schema.getId().isSingleton();
        for (org.deephacks.tools4j.config.model.Schema.SchemaProperty prop : schema
                .get(org.deephacks.tools4j.config.model.Schema.SchemaProperty.class)) {
            property.add(new SchemaProperty(prop));
            propertyNames.add(prop.getName());
        }

        for (org.deephacks.tools4j.config.model.Schema.SchemaPropertyList prop : schema
                .get(org.deephacks.tools4j.config.model.Schema.SchemaPropertyList.class)) {
            propertyList.add(new SchemaPropertyList(prop));
            propertyNames.add(prop.getName());
        }

        for (org.deephacks.tools4j.config.model.Schema.SchemaPropertyRef prop : schema
                .get(org.deephacks.tools4j.config.model.Schema.SchemaPropertyRef.class)) {
            propertyRef.add(new SchemaPropertyRef(prop));
            propertyNames.add(prop.getName());
        }

        for (org.deephacks.tools4j.config.model.Schema.SchemaPropertyRefList prop : schema
                .get(org.deephacks.tools4j.config.model.Schema.SchemaPropertyRefList.class)) {
            propertyRefList.add(new SchemaPropertyRefList(prop));
            propertyNames.add(prop.getName());
        }

        for (org.deephacks.tools4j.config.model.Schema.SchemaPropertyRefMap prop : schema
                .get(org.deephacks.tools4j.config.model.Schema.SchemaPropertyRefMap.class)) {
            propertyRefMap.add(new SchemaPropertyRefMap(prop));
            propertyNames.add(prop.getName());
        }
    }

    public String getIdName() {
        return idName;
    }

    public void setIdName(String idName) {
        this.idName = idName;
    }

    public String getIdDesc() {
        return idDesc;
    }

    public void setIdDesc(String idDesc) {
        this.idDesc = idDesc;
    }

    public List<String> getPropertyNames() {
        return propertyNames;
    }

    public void setPropertyNames(List<String> propertyNames) {
        this.propertyNames = propertyNames;
    }

    public List<SchemaProperty> getProperty() {
        return property;
    }

    public void setProperty(List<SchemaProperty> property) {
        this.property = property;
    }

    public List<SchemaPropertyList> getPropertyList() {
        return propertyList;
    }

    public void setPropertyList(List<SchemaPropertyList> propertyList) {
        this.propertyList = propertyList;
    }

    public List<SchemaPropertyRef> getPropertyRef() {
        return propertyRef;
    }

    public void setPropertyRef(List<SchemaPropertyRef> propertyRef) {
        this.propertyRef = propertyRef;
    }

    public List<SchemaPropertyRefList> getPropertyRefList() {
        return propertyRefList;
    }

    public void setPropertyRefList(List<SchemaPropertyRefList> propertyRefList) {
        this.propertyRefList = propertyRefList;
    }

    public List<SchemaPropertyRefMap> getPropertyRefMap() {
        return propertyRefMap;
    }

    public void setPropertyRefMap(List<SchemaPropertyRefMap> propertyRefMap) {
        this.propertyRefMap = propertyRefMap;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String name) {
        this.schemaName = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String type) {
        this.className = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public boolean isSingleton() {
        return singleton;
    }

    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    public static class AbstractSchemaProperty {
        private String name;
        private String desc;
        private String fieldName;
        private boolean isImmutable;

        public AbstractSchemaProperty(){

        }
        public AbstractSchemaProperty(
                org.deephacks.tools4j.config.model.Schema.AbstractSchemaProperty schema) {
            this.name = schema.getName();
            this.desc = schema.getDesc();
            this.fieldName = schema.getFieldName();
            this.isImmutable = schema.isImmutable();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public boolean isImmutable() {
            return isImmutable;
        }

        public void setImmutable(boolean isImmutable) {
            this.isImmutable = isImmutable;
        }

    }

    public final static class SchemaProperty extends AbstractSchemaProperty {
        private String defaultValue;
        private String type;
        private List<String> enums;

        public SchemaProperty(){

        }

        public SchemaProperty(org.deephacks.tools4j.config.model.Schema.SchemaProperty schema) {
            super(schema);
            this.defaultValue = schema.getDefaultValue();
            this.type = schema.getType();
            this.enums = schema.getEnums();
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isEnum() {
            return enums != null && enums.size() > 0;
        }

        public List<String> getEnums() {
            return enums;
        }

        public void setEnums(List<String> enums) {
            this.enums = enums;
        }

    }

    public final static class SchemaPropertyList extends AbstractSchemaProperty {
        private String type;
        private String collectionType;
        private List<String> defaultValues;
        private List<String> enums;

        public SchemaPropertyList(){

        }

        public SchemaPropertyList(
                org.deephacks.tools4j.config.model.Schema.SchemaPropertyList schema) {
            super(schema);
            this.type = schema.getType();
            this.collectionType = schema.getCollectionType();
            this.defaultValues = schema.getDefaultValues();
            this.enums = schema.getEnums();
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getCollectionType() {
            return collectionType;
        }

        public void setCollectionType(String collectionType) {
            this.collectionType = collectionType;
        }

        public List<String> getDefaultValues() {
            return defaultValues;
        }

        public void setDefaultValues(List<String> defaultValues) {
            this.defaultValues = defaultValues;
        }

        public boolean isEnum() {
            return enums != null && enums.size() > 0;
        }

        public List<String> getEnums() {
            return enums;
        }

        public void setEnums(List<String> enums) {
            this.enums = enums;
        }

    }

    public final static class SchemaPropertyRef extends AbstractSchemaProperty {
        private String schemaName;
        private boolean isSingleton;

        public SchemaPropertyRef(){

        }

        public SchemaPropertyRef(org.deephacks.tools4j.config.model.Schema.SchemaPropertyRef schema) {
            super(schema);
            this.schemaName = schema.getSchemaName();
            this.isSingleton = schema.isSingleton();
        }

        public String getSchemaName() {
            return schemaName;
        }

        public void setSchemaName(String schemaName) {
            this.schemaName = schemaName;
        }

        public boolean isSingleton() {
            return isSingleton;
        }

        public void setSingleton(boolean isSingleton) {
            this.isSingleton = isSingleton;
        }

    }

    public final static class SchemaPropertyRefList extends AbstractSchemaProperty {
        private String collectionType;
        private String schemaName;

        public SchemaPropertyRefList(){

        }

        public SchemaPropertyRefList(
                org.deephacks.tools4j.config.model.Schema.SchemaPropertyRefList schema) {
            super(schema);
            this.schemaName = schema.getSchemaName();
            this.collectionType = schema.getCollectionType();
        }

        public String getCollectionType() {
            return collectionType;
        }

        public void setCollectionType(String collectionType) {
            this.collectionType = collectionType;
        }

        public String getSchemaName() {
            return schemaName;
        }

        public void setSchemaName(String schemaName) {
            this.schemaName = schemaName;
        }

    }

    public final static class SchemaPropertyRefMap extends AbstractSchemaProperty {
        private String mapType;
        private String schemaName;

        public SchemaPropertyRefMap(){

        }

        public SchemaPropertyRefMap(
                org.deephacks.tools4j.config.model.Schema.SchemaPropertyRefMap schema) {
            super(schema);
            this.schemaName = schema.getSchemaName();
            this.mapType = schema.getMapType();
        }

        public String getMapType() {
            return mapType;
        }

        public void setMapType(String mapType) {
            this.mapType = mapType;
        }

        public String getSchemaName() {
            return schemaName;
        }

        public void setSchemaName(String schemaName) {
            this.schemaName = schemaName;
        }

    }
}
