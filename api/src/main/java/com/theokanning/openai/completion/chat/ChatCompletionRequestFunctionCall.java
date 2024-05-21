package com.theokanning.openai.completion.chat;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
@JsonSerialize(using = ChatCompletionRequestFunctionCall.Serializer.class)
@JsonDeserialize(using = ChatCompletionRequestFunctionCall.Deserializer.class)
public class ChatCompletionRequestFunctionCall {
    /**
     * Controls which (if any) function is called by the model.
     * none means the model will not call a function and instead generates a message.
     * auto means the model can pick between generating a message or calling a function.
     * Specifying a particular function via {"name": "my_function"} forces the model to call that function.
     */

    public static final ChatCompletionRequestFunctionCall NONE = new ChatCompletionRequestFunctionCall("none");

    public static final ChatCompletionRequestFunctionCall AUTO = new ChatCompletionRequestFunctionCall("auto");

    String name;

    public static ChatCompletionRequestFunctionCall of(String name) {
        return new ChatCompletionRequestFunctionCall(name);
    }


    public static class Serializer extends JsonSerializer<ChatCompletionRequestFunctionCall> {
        @Override
        public void serialize(ChatCompletionRequestFunctionCall value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            String name = value.getName();
            if ("none".equals(name) || "auto".equals(name)) {
                gen.writeString(name);
                return;
            }
            if (name == null) {
                gen.writeNull();
            } else {
                gen.writeStartObject();
                gen.writeObjectField("name", name);
                gen.writeEndObject();
            }
        }
    }

    public static class Deserializer extends JsonDeserializer<ChatCompletionRequestFunctionCall> {
        @Override
        public ChatCompletionRequestFunctionCall deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            //如果是字符串,则读取字符串
            if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
                String text = p.getText();
                switch (text) {
                    case "none":
                        return ChatCompletionRequestFunctionCall.NONE;
                    case "auto":
                        return ChatCompletionRequestFunctionCall.AUTO;
                    default:
                        return ChatCompletionRequestFunctionCall.of(text);
                }
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
