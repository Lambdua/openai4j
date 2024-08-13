package com.theokanning.openai.completion.chat;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.kjetland.jackson.jsonSchema.JsonSchemaConfig;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;
import com.theokanning.openai.utils.JsonUtil;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * see {@link ChatCompletionRequest} documentation.
 */
@Data
@NoArgsConstructor
public class ChatResponseFormat {
	private static final ObjectMapper MAPPER = JsonUtil.getInstance();
	private static final JsonSchemaConfig CONFIG = JsonSchemaConfig.vanillaJsonSchemaDraft4();
	private static final JsonSchemaGenerator JSON_SCHEMA_GENERATOR = new JsonSchemaGenerator(MAPPER, CONFIG);
	
    /**
     * auto/text/json_object
     */
    private String type;
    
    /**
     * This is used together with type field set to "json_schema"
     * to enable structured outputs.
     * 
     * @see https://openai.com/index/introducing-structured-outputs-in-the-api/
     * 
     */
    private JsonNode json_schema;

    /**
     * 构造私有,只允许从静态变量获取
     */
    private ChatResponseFormat(String type) {
        this.type = type;
    }

    public static final ChatResponseFormat AUTO = new ChatResponseFormat("auto");

    public static final ChatResponseFormat TEXT = new ChatResponseFormat("text");

    public static final ChatResponseFormat JSON_OBJECT = new ChatResponseFormat("json_object");
    
    public static ChatResponseFormat jsonSchema(Class<?> rootClass) {
	    JsonNode jsonSchema = JSON_SCHEMA_GENERATOR.generateJsonSchema(rootClass);
	    ChatResponseFormat jsonSchemaFormat = new ChatResponseFormat("json_schema");
	    jsonSchemaFormat.setJson_schema(jsonSchema);
	    return jsonSchemaFormat;
	}

    @NoArgsConstructor
    public static class ChatResponseFormatSerializer extends JsonSerializer<ChatResponseFormat> {
        @Override
        public void serialize(ChatResponseFormat value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value.getType().equals("auto")) {
                gen.writeString(value.getType());
            } else {
                gen.writeStartObject();
                gen.writeObjectField("type", (value).getType());
                
                if (value.getType().equals("json_schema")) {
                    JsonNode jsonSchema = value.getJson_schema();

                    gen.writeObjectFieldStart("json_schema");
					gen.writeStringField("name", "ChatResponseFormat");
                    gen.writeBooleanField("strict", true);
                    gen.writeFieldName("schema");
					gen.writeTree(jsonSchema);
                    gen.writeEndObject();
                }
                
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
