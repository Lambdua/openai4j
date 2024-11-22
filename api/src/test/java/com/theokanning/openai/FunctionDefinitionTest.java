package com.theokanning.openai;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.function.FunctionDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FunctionDefinitionTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    class TestParameters {
        public String name;
    }

    @Test
    void shouldSerializeWithStrictTrue() throws Exception {
        // given
        FunctionDefinition function =
                FunctionDefinition.builder()
                        .name("test_function")
                        .description("Test function")
                        .strict(true)
                        .build();

        // when
        String json = objectMapper.writeValueAsString(function);
        JsonNode jsonNode = objectMapper.readTree(json);

        // then
        assertEquals(jsonNode.get("name").asText(), "test_function");
        assertEquals(jsonNode.get("description").asText(),"Test function");
        assertTrue(jsonNode.get("strict").asBoolean());
    }

    @Test
    void shouldSerializeWithStrictFalse() throws Exception {
        // given
        FunctionDefinition function =
                FunctionDefinition.builder()
                        .name("test_function")
                        .description("Test function")
                        .strict(false)
                        .build();

        // when
        String json = objectMapper.writeValueAsString(function);
        JsonNode jsonNode = objectMapper.readTree(json);

        // then
        assertEquals("test_function", jsonNode.get("name").asText());
        assertEquals("Test function", jsonNode.get("description").asText());
        assertFalse(jsonNode.get("strict").asBoolean());
    }

    @Test
    void shouldSerializeWithoutStrict() throws Exception {
        // given
        FunctionDefinition function =
                FunctionDefinition.builder().name("test_function").description("Test function").build();

        // when
        String json = objectMapper.writeValueAsString(function);
        JsonNode jsonNode = objectMapper.readTree(json);

        // then
        assertEquals("test_function", jsonNode.get("name").asText());
        assertEquals("Test function", jsonNode.get("description").asText());
        assertFalse(jsonNode.has("strict"));
    }

    @Test
    void shouldSetAdditionalPropertiesFalseWhenStrictIsTrue() throws Exception {
        // given
        FunctionDefinition function =
                FunctionDefinition.<TestParameters>builder()
                        .name("test_function")
                        .description("Test function")
                        .strict(true)
                        .parametersDefinitionByClass(TestParameters.class)
                        .build();

        // when
        String json = objectMapper.writeValueAsString(function);
        JsonNode jsonNode = objectMapper.readTree(json);
        JsonNode parametersNode = jsonNode.get("parameters");

        // then
        assertFalse(parametersNode.get("additionalProperties").asBoolean());
    }

    @Test
    void whenStrictIsFalse() throws Exception {
        // given
        FunctionDefinition function =
                FunctionDefinition.<TestParameters>builder()
                        .name("test_function")
                        .description("Test function")
                        .strict(false)
                        .parametersDefinitionByClass(TestParameters.class)
                        .build();

        // when
        String json = objectMapper.writeValueAsString(function);
        JsonNode jsonNode = objectMapper.readTree(json);
        JsonNode parametersNode = jsonNode.get("parameters");

        // then
        assertFalse(parametersNode.has("additionalProperties"));
    }

    @Test
    void shouldNotSetAdditionalPropertiesWhenStrictIsNull() throws Exception {
        // given
        FunctionDefinition function =
                FunctionDefinition.<TestParameters>builder()
                        .name("test_function")
                        .description("Test function")
                        .parametersDefinitionByClass(TestParameters.class)
                        .build();

        // when
        String json = objectMapper.writeValueAsString(function);
        JsonNode jsonNode = objectMapper.readTree(json);
        JsonNode parametersNode = jsonNode.get("parameters");

        // then
        assertFalse(parametersNode.has("additionalProperties"));
    }
}
