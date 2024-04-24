package com.theokanning.openai.completion.chat;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.TextNode;

import java.io.IOException;

public class ChatFunctionCallArgumentsSerializerAndDeserializer {

    private final static ObjectMapper MAPPER = new ObjectMapper();

    private ChatFunctionCallArgumentsSerializerAndDeserializer() {
    }

    public static class Serializer extends JsonSerializer<JsonNode> {

        private Serializer() {
        }

        @Override
        public void serialize(JsonNode value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
            } else {
                gen.writeString(value instanceof TextNode ? value.asText() : value.toString());
            }
        }
    }

    public static class Deserializer extends JsonDeserializer<JsonNode> {

        private Deserializer() {
        }

        @Override
        public JsonNode deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String json = p.getValueAsString();
            if (json == null || p.currentToken() == JsonToken.VALUE_NULL) {
                return null;
            }

            if (!isValidJson(json)) {
                // encode to valid JSON escape otherwise we will lose quotes
                json = MAPPER.writeValueAsString(json);
            }

            try {
                JsonNode node = null;
                try {
                    node = MAPPER.readTree(json);
                } catch (JsonParseException ignored) {
                }
                if (node == null || node.getNodeType() == JsonNodeType.MISSING) {
                    node = MAPPER.readTree(p);
                }
                return node;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }

        private boolean isValidJson(String jsonString) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode tree = objectMapper.readTree(jsonString);
                return tree != null && (tree.isObject() || tree.isArray());
            } catch (JsonProcessingException e) {
                return false;
            }
        }
    }

}
