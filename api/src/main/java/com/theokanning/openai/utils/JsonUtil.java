package com.theokanning.openai.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

/**
 * @author LiangTao
 * @date 2024年05月24 15:02
 **/
public class JsonUtil {
    private static final ObjectMapper MAPPER = ObjectMapperHolder.MAPPER;

    public static ObjectMapper getInstance() {
        return MAPPER;
    }

    public static String writeValueAsString(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static  boolean isValidJson(String jsonString) {
        try {
            JsonNode tree = MAPPER.readTree(jsonString);
            return tree != null && (tree.isObject() || tree.isArray());
        } catch (JsonProcessingException e) {
            return false;
        }
    }


    public static <T> T readValue(String content, Class<T> valueType) {
        try {
            return MAPPER.readValue(content, valueType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static class ObjectMapperHolder {
        private static final ObjectMapper MAPPER = new ObjectMapper();
        static {
            MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            MAPPER.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
        }
    }


}
