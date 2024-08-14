package com.theokanning.openai.function;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kjetland.jackson.jsonSchema.JsonSchemaConfig;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;
import com.theokanning.openai.utils.JsonUtil;

import java.io.IOException;

public class FunctionParametersSerializer extends JsonSerializer<FunctionDefinition> {
    private final JsonSchemaConfig config = JsonSchemaConfig.vanillaJsonSchemaDraft4();

    private final JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(JsonUtil.getInstance(), config);

    @Override
    public void serialize(FunctionDefinition value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("name", value.getName());
        gen.writeStringField("description", value.getDescription());
        if (value.getStrict()!=null){
            gen.writeBooleanField("strict", value.getStrict());
        }
        if (value.getParametersDefinitionClass() != null) {
            gen.writeFieldName("parameters");
            ObjectNode parameterSchema = (ObjectNode) jsonSchemaGenerator.generateJsonSchema(value.getParametersDefinitionClass());
            parameterSchema.remove("$schema");
            parameterSchema.remove("title");
            parameterSchema.remove("additionalProperties");
            gen.writeRawValue(JsonUtil.writeValueAsString(parameterSchema));
        } else {
            gen.writeFieldName("parameters");
            Object parametersDefinition = value.getParametersDefinition();
            if (parametersDefinition instanceof String && JsonUtil.isValidJson((String) parametersDefinition)){
                String prettyString = JsonUtil.getInstance().readTree((String) parametersDefinition).toPrettyString();
                gen.writeRawValue(prettyString);
            }else {
                gen.writeRawValue(JsonUtil.writeValueAsString(parametersDefinition));
            }
        }
        gen.writeEndObject();
    }




}





