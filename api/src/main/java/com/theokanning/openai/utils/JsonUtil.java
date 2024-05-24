package com.theokanning.openai.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author LiangTao
 * @date 2024年05月24 15:02
 **/
public class JsonUtil {
    private static final ObjectMapper mapper = ObjectMapperHolder.mapper;

    public static ObjectMapper getInstance() {
        return mapper;
    }

    public static String writeValueAsString(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readValue(String content, Class<T> valueType) {
        try {
            return mapper.readValue(content, valueType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static class ObjectMapperHolder {
        private static final ObjectMapper mapper = new ObjectMapper();
    }


}
