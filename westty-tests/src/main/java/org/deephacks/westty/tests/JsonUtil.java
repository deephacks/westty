package org.deephacks.westty.tests;


import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.VisibilityChecker;
import org.codehaus.jackson.map.type.CollectionType;

import java.util.List;

public class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(
                JsonAutoDetect.Visibility.ANY));
    }

    public static List<Integer> getPorts(String json){
        try {
            CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, Integer.class);
            return mapper.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String toJson(JsonEntity object){
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonEntity fromJson(String json){
        try {
            return mapper.readValue(json, JsonEntity.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String toJsonList(List<JsonEntity> list){
        try {
            return mapper.writeValueAsString(list);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<JsonEntity> fromJsonList(String json){
        try {
            CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class, JsonEntity.class);
            return mapper.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
