package example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kjetland.jackson.jsonSchema.JsonSchemaConfig;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;
import com.theokanning.openai.assistants.assistant.FunctionTool;
import com.theokanning.openai.completion.chat.ChatFunction;
import com.theokanning.openai.completion.chat.ChatFunctionDynamic;
import com.theokanning.openai.completion.chat.ChatFunctionProperty;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

/**
 * severalWaysToCreateFunctionTool
 *
 * @author LiangTao
 * @date 2024年05月10 10:30
 **/
public class FunctionOrToolCreateExample {
    static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws JsonProcessingException {
        // createByJsonString();
        createByDynamic();
    }

    /**
     * create function by dynamic
     */
    static void createByDynamic() throws JsonProcessingException {
        //assistant function
        ChatFunctionDynamic dynamicFun = ChatFunctionDynamic.builder()
                .name("weather_reporter")
                .description("Get the current weather of a location")
                .addProperty(ChatFunctionProperty.builder()
                        .name("location")
                        .type("string")
                        .description("The city and state, e.g. San Francisco, CA")
                        .build())
                .addProperty(ChatFunctionProperty.builder()
                        .name("unit")
                        .type("string")
                        .enumValues(new HashSet<>(Arrays.asList("celsius", "fahrenheit")))
                        .build())
                .build();

        final ObjectMapper mapper = new ObjectMapper();
        final JsonSchemaConfig config = JsonSchemaConfig.vanillaJsonSchemaDraft4();
        final JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(mapper, config);
        System.out.println(jsonSchemaGenerator.generateJsonSchema(ToolUtil.Weather.class));
        System.out.println("----");

        FunctionTool functionTool = new FunctionTool(dynamicFun);
        System.out.println(mapper.writeValueAsString(functionTool));
    }

    /**
     *  create functionTool by ChatFunctionDynamic
     */
    static void createByJsonString() throws JsonProcessingException {
        //this function definition is not recommended, it is recommended to use the ToolUtil class to define the function
        String funcDefJson = "{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"location\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"The city and state, e.g. San Francisco, CA\"\n" +
                "    },\n" +
                "    \"unit\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"enum\": [\"celsius\", \"fahrenheit\"]\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"location\"]\n" +
                "}";
        Map<String, Object> funcParameters = mapper.readValue(funcDefJson, new TypeReference<Map<String, Object>>() {
        });

        //assistant function
        FunctionTool functionTool = new FunctionTool(funcParameters);
        System.out.println(mapper.writeValueAsString(functionTool));




    }

}
