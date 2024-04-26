package com.theokanning.openai.completion.chat;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;

/**
 * see {@link ChatCompletionRequest} documentation.
 */
@Data
public class ChatResponseFormat {
    /**
     * auto/text/json_object
     */
    private String type;

    /**
     * 构造私有,只允许从静态变量获取
     */
    private ChatResponseFormat(String type) {
        this.type = type;
    }

    public static final ChatResponseFormat AUTO = new ChatResponseFormat("auto");

    public static final ChatResponseFormat TEXT = new ChatResponseFormat("text");

    public static final ChatResponseFormat JSON_OBJECT = new ChatResponseFormat("json_object");


    @NoArgsConstructor
    public static class ChatResponseFormatSerializer extends JsonSerializer<ChatResponseFormat> {
        @Override
        public void serialize(ChatResponseFormat value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value.getType().equals("auto")) {
                gen.writeString(value.getType());
            } else {
                gen.writeStartObject();
                gen.writeObjectField("type", (value).getType());
                gen.writeEndObject();
            }
        }

    }

    @NoArgsConstructor
    public static class ChatResponseFormatDeserializer extends JsonDeserializer<ChatResponseFormat> {
        @Override
        public ChatResponseFormat deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
            if (jsonParser.getCurrentToken() == JsonToken.VALUE_STRING) {
                String text = jsonParser.getText();
                if (!"auto".equals(text)) {
                    throw new InvalidFormatException(jsonParser, "Invalid response format", jsonParser.getCurrentToken().toString(), ChatResponseFormat.class);
                }
                return new ChatResponseFormat(text);
            }
            // 处理对象的情况 return ChatResponseFormat
            if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
                while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                    if (jsonParser.getCurrentName().equals("type")) {
                        jsonParser.nextToken();
                        switch (jsonParser.getText()){
                            case "auto":
                                return AUTO;
                            case "text":
                                return TEXT;
                            case "json_object":
                                return JSON_OBJECT;
                            default:
                                throw new InvalidFormatException(jsonParser, "Invalid response format", jsonParser.getCurrentToken().toString(), ChatResponseFormat.class);
                        }
                    }
                }
            }
            throw new InvalidFormatException(jsonParser, "Invalid response format", jsonParser.getCurrentToken().toString(), ChatResponseFormat.class);
        }
    }
}
