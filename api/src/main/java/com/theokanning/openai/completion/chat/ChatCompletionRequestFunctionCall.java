package com.theokanning.openai.completion.chat;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;

/**
 * @author LiangTao
 * @date 2024年04月24 15:36
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatCompletionRequestFunctionCall {
    String name;

    public static class Serializer extends JsonSerializer<Object> {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value instanceof String) {
                gen.writeString((String) value);
                return;
            }
            if (value instanceof ChatCompletionRequestFunctionCall) {
                ChatCompletionRequestFunctionCall functionCall = (ChatCompletionRequestFunctionCall) value;
                if (functionCall.getName() == null) {
                    gen.writeNull();
                } else {
                    gen.writeStartObject();
                    gen.writeObjectField("name", functionCall.getName());
                    gen.writeEndObject();
                }
                return;
            }
            // This should never happen
            throw new IllegalArgumentException("Unexpected value to function call: " + value);
        }
    }

    public static class Deserializer extends JsonDeserializer<Object> {
        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            //如果是字符串,则读取字符串
            if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
                return p.getText();
            }
            //如果是对象,则读取对象
            if (p.getCurrentToken() == JsonToken.START_OBJECT) {
                String name = null;
                while (p.nextToken() != JsonToken.END_OBJECT) {
                    if ("name".equals(p.getCurrentName())) {
                        p.nextToken();
                        name = p.getValueAsString();
                    }
                }
                return new ChatCompletionRequestFunctionCall(name);
            }
            return null;
        }
    }
}
