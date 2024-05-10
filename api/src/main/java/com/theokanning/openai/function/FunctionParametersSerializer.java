package com.theokanning.openai.function;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.kjetland.jackson.jsonSchema.JsonSchemaConfig;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;

import java.io.IOException;

public class FunctionParametersSerializer extends JsonSerializer<Class<?>> {

    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonSchemaConfig config = JsonSchemaConfig.vanillaJsonSchemaDraft4();
    private final JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(mapper, config);

    @Override
    public void serialize(Class<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

    }



}





