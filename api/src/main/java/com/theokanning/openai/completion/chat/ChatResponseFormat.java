package com.theokanning.openai.completion.chat;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;

/**
 * see {@link ChatCompletionRequest} documentation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseFormat {
    private ResponseFormat type;

    public enum ResponseFormat {
        TEXT("text"),
        JSON("json_object");

        @JsonValue
        private final String value;

        ResponseFormat(final String value) {
            this.value = value;
        }

        @JsonValue
        public String value() {
            return value;
        }
    }


    @NoArgsConstructor
    public static class ChatResponseFormatSerializer extends JsonSerializer<Object> {
        @Override
        public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            if (o instanceof String) {
                jsonGenerator.writeString((String) o);
            }
            if (o instanceof ChatResponseFormat) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeObjectField("type", ((ChatResponseFormat) o).getType());
                jsonGenerator.writeEndObject();
            }
        }
    }

    @NoArgsConstructor
    public static class ChatResponseFormatDeserializer extends JsonDeserializer<Object> {
        @Override
        public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
            if (jsonParser.getCurrentToken() == JsonToken.VALUE_STRING) {
                return jsonParser.getText();
            }
            // 处理对象的情况 return ChatResponseFormat
            if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
                ChatResponseFormat chatResponseFormat = new ChatResponseFormat();
                while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                    if (jsonParser.getCurrentName().equals("type")) {
                        jsonParser.nextToken();
                        chatResponseFormat.setType(ResponseFormat.valueOf(jsonParser.getText().toUpperCase()));
                    }
                }
                return chatResponseFormat;
            }
            throw new InvalidFormatException(jsonParser, "Invalid response format", jsonParser.getCurrentToken().toString(), ChatResponseFormat.class);
        }
    }
}
