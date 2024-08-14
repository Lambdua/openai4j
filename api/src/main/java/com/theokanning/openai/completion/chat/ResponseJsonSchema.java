package com.theokanning.openai.completion.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kjetland.jackson.jsonSchema.JsonSchemaConfig;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;
import com.theokanning.openai.utils.JsonUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.IOException;

/**
 * @author LiangTao
 * @date 2024年08月14 10:57
 **/
@JsonSerialize(using = ResponseJsonSchema.ResponseJsonSchemaSerializer.class)
@NoArgsConstructor
@Data
public class ResponseJsonSchema {
    /**
     * The name of the function being called.
     */
    @NonNull
    protected String name;

    private boolean strict = true;

    /**
     * parameters definition by class schema ,will use {@link JsonSchemaGenerator} to generate json schema
     */
    private Class<?> schemaClass;

    /**
     * The parameters the functions accepts.Choose between this parameter and {@link #schemaClass}
     * This parameter requires you to implement the serialization/deserialization logic of the JSON schema yourself.
     **/
    @JsonProperty("schema")
    private Object schemaDefinition;


    public static class ResponseJsonSchemaSerializer extends JsonSerializer<ResponseJsonSchema> {
        private final JsonSchemaConfig config = JsonSchemaConfig.vanillaJsonSchemaDraft4();

        private final JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(JsonUtil.getInstance(), config);

        @Override
        public void serialize(ResponseJsonSchema value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("name", value.getName());
            gen.writeBooleanField("strict", value.isStrict());
            if (value.getSchemaClass() != null) {
                gen.writeFieldName("schema");
                ObjectNode parameterSchema = (ObjectNode) jsonSchemaGenerator.generateJsonSchema(value.getSchemaClass());
                parameterSchema.remove("$schema");
                parameterSchema.remove("title");
                gen.writeRawValue(JsonUtil.writeValueAsString(parameterSchema));
            } else {
                gen.writeFieldName("schema");
                Object parametersDefinition = value.getSchemaDefinition();
                if (parametersDefinition instanceof String && JsonUtil.isValidJson((String) parametersDefinition)) {
                    String prettyString = JsonUtil.getInstance().readTree((String) parametersDefinition).toPrettyString();
                    gen.writeRawValue(prettyString);
                } else {
                    gen.writeRawValue(JsonUtil.writeValueAsString(parametersDefinition));
                }
            }
            gen.writeEndObject();
        }
    }

    public static <T> ResponseJsonSchema.Builder<T> builder() {
        return new ResponseJsonSchema.Builder<>();
    }

    public static class Builder<T> {
        private String name;
        private Class<T> schemaClass;

        private T schemaDefinition;

        private boolean strict = true;

        public ResponseJsonSchema.Builder<T> name(String name) {
            this.name = name;
            return this;
        }

        public ResponseJsonSchema.Builder<T> strict(boolean strict) {
            this.strict = strict;
            return this;
        }

        public ResponseJsonSchema.Builder<T> schemaClass(Class<T> schemaClass) {
            this.schemaClass = schemaClass;
            return this;
        }

        public ResponseJsonSchema.Builder<T> schemaDefinition(T schemaDefinition) {
            this.schemaDefinition = schemaDefinition;
            return this;
        }

        public ResponseJsonSchema build() {
            if (name == null) {
                throw new IllegalArgumentException("name can't be null");
            }

            if (schemaDefinition != null && schemaClass != null) {
                throw new IllegalArgumentException("schemaClass and schemaDefinition can't be set at the same time,please set one of them");
            }
            ResponseJsonSchema responseJsonSchema = new ResponseJsonSchema();
            responseJsonSchema.name = name;
            responseJsonSchema.schemaClass = schemaClass;
            responseJsonSchema.schemaDefinition = schemaDefinition;
            responseJsonSchema.strict = strict;
            return responseJsonSchema;
        }
    }


}
