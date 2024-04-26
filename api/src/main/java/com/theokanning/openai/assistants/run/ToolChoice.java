package com.theokanning.openai.assistants.run;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.Data;

import java.io.IOException;

/**
 * @author LiangTao
 * @date 2024年04月18 17:18
 **/
@Data
public class ToolChoice {
    /**
     * The type of the tool. If type is function, the function name must be set
     * enum: none/auto/function
     */
    String type;

    /**
     * The name of the function to call.
     */
    Function function;

    public static final ToolChoice NONE = new ToolChoice("none");

    public static final ToolChoice AUTO = new ToolChoice("auto");

    private ToolChoice(String type) {
        this.type = type;
    }

    public ToolChoice(Function function) {
        this.type = "function";
        if (function == null) {
            throw new IllegalArgumentException("Function must not be null");
        }
        this.function = function;
    }

    public static class Deserializer extends JsonDeserializer<ToolChoice> {

        @Override
        public ToolChoice deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            if (jsonParser.getCurrentToken() == JsonToken.VALUE_STRING) {
                String type = jsonParser.getText();
                switch (type) {
                    case "none":
                        return ToolChoice.NONE;
                    case "auto":
                        return ToolChoice.AUTO;
                    default:
                        return new ToolChoice(type);
                }
            }
            if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
                // 处理对象的情况
                while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                    if (jsonParser.getCurrentName().equals("function")) {
                        jsonParser.nextToken();
                        return new ToolChoice(parseFunction(jsonParser));
                    }
                }
            }
            //抛出异常
            throw new IllegalArgumentException("Invalid ToolChoice");
        }

        private Function parseFunction(JsonParser jsonParser) throws IOException {
            if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
                // 处理对象的情况
                Function function = new Function();
                while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                    // 判断对象内元素类型并进行相应的反序列化
                    if (jsonParser.getCurrentName().equals("name")) {
                        function.setName(jsonParser.nextTextValue());
                    }
                }
                jsonParser.nextToken();
                return function;
            }
            //抛出异常
            throw new IllegalArgumentException("Invalid Function");
        }
    }

    public static class Serializer extends JsonSerializer<ToolChoice> {
        @Override
        public void serialize(ToolChoice toolChoice, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            String type = toolChoice.getType();
            switch (type) {
                case "none":
                    jsonGenerator.writeString(type);
                    break;
                case "auto":
                    jsonGenerator.writeString(type);
                    break;
                default:
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeStringField("type", type);
                    if (toolChoice.getType().equals("function")) {
                        jsonGenerator.writeObjectField("function", toolChoice.getFunction());
                    }
                    jsonGenerator.writeEndObject();
            }
        }
    }
}
