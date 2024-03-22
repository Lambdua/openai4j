package com.theokanning.openai.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.theokanning.openai.completion.chat.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ChatCompletionTest {
    static class City {
        @JsonPropertyDescription("The time to get the cities")
        public String time;
    }

    static class Weather {
        @JsonPropertyDescription("City and state, for example: León, Guanajuato")
        public String location;

        @JsonPropertyDescription("The temperature unit, can be 'celsius' or 'fahrenheit'")
        @JsonProperty(required = true)
        public WeatherUnit unit;
    }

    enum WeatherUnit {
        CELSIUS, FAHRENHEIT;
    }

    static class WeatherResponse {
        public String location;
        public WeatherUnit unit;
        public int temperature;
        public String description;

        public WeatherResponse(String location, WeatherUnit unit, int temperature, String description) {
            this.location = location;
            this.unit = unit;
            this.temperature = temperature;
            this.description = description;
        }
    }

    String token = System.getenv("OPENAI_TOKEN");
    OpenAiService service = new OpenAiService(token);

    @Test
    void createChatCompletion() {
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), "You are a dog and will speak as such.");
        messages.add(systemMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .n(5)
                .maxTokens(50)
                .logitBias(new HashMap<>())
                .build();

        List<ChatCompletionChoice> choices = service.createChatCompletion(chatCompletionRequest).getChoices();
        assertEquals(5, choices.size());
    }

    @Test
    void streamChatCompletion() {
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), "You are a dog and will speak as such.");
        messages.add(systemMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .n(1)
                .maxTokens(50)
                .logitBias(new HashMap<>())
                .stream(true)
                .build();

        List<ChatCompletionChunk> chunks = new ArrayList<>();
        service.streamChatCompletion(chatCompletionRequest).blockingForEach(chunks::add);
        assertTrue(chunks.size() > 0);
        assertNotNull(chunks.get(0).getChoices().get(0));
    }

    @Test
    void createChatCompletionWithJsonMode() {
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), "You will generate a random name and return it in JSON format.");
        messages.add(systemMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo-1106")
                .messages(messages)
                .responseFormat(ChatResponseFormat.builder().type(ChatResponseFormat.ResponseFormat.JSON).build())
                .maxTokens(50)
                .logitBias(new HashMap<>())
                .build();

        ChatCompletionChoice choice = service.createChatCompletion(chatCompletionRequest).getChoices().get(0);
        assertTrue(isValidJson(choice.getMessage().getContent()), "Response is not valid JSON");
    }

    private boolean isValidJson(String jsonString) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.readTree(jsonString);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    @Test
    void createChatCompletionWithFunctions() {
        final List<ChatFunction> functions = Collections.singletonList(ChatFunction.builder()
                .name("get_weather")
                .description("Get the current weather in a given location")
                .executor(Weather.class, w -> new WeatherResponse(w.location, w.unit, 25, "sunny"))
                .build());
        final FunctionExecutor functionExecutor = new FunctionExecutor(functions);

        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), "You are a helpful assistant.");
        final ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), "What is the weather in Monterrey, Nuevo León?");
        messages.add(systemMessage);
        messages.add(userMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo-0613")
                .messages(messages)
                .functions(functionExecutor.getFunctions())
                .n(1)
                .maxTokens(100)
                .logitBias(new HashMap<>())
                .build();

        ChatCompletionChoice choice = service.createChatCompletion(chatCompletionRequest).getChoices().get(0);
        assertEquals("function_call", choice.getFinishReason());
        assertNotNull(choice.getMessage().getFunctionCall());
        assertEquals("get_weather", choice.getMessage().getFunctionCall().getName());
        assertInstanceOf(ObjectNode.class, choice.getMessage().getFunctionCall().getArguments());

        ChatMessage callResponse = functionExecutor.executeAndConvertToMessageHandlingExceptions(choice.getMessage().getFunctionCall());
        assertNotEquals("error", callResponse.getName());

        // this performs an unchecked cast
        WeatherResponse functionExecutionResponse = functionExecutor.execute(choice.getMessage().getFunctionCall());
        assertInstanceOf(WeatherResponse.class, functionExecutionResponse);
        assertEquals(25, functionExecutionResponse.temperature);

        JsonNode jsonFunctionExecutionResponse = functionExecutor.executeAndConvertToJson(choice.getMessage().getFunctionCall());
        assertInstanceOf(ObjectNode.class, jsonFunctionExecutionResponse);
        assertEquals("25", jsonFunctionExecutionResponse.get("temperature").asText());

        messages.add(choice.getMessage());
        messages.add(callResponse);

        ChatCompletionRequest chatCompletionRequest2 = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo-0613")
                .messages(messages)
                .functions(functionExecutor.getFunctions())
                .n(1)
                .maxTokens(100)
                .logitBias(new HashMap<>())
                .build();

        ChatCompletionChoice choice2 = service.createChatCompletion(chatCompletionRequest2).getChoices().get(0);
        assertNotEquals("function_call", choice2.getFinishReason()); // could be stop or length, but should not be function_call
        assertNull(choice2.getMessage().getFunctionCall());
        assertNotNull(choice2.getMessage().getContent());
    }

    @Test
    void createChatCompletionWithDynamicFunctions() {
        ChatFunctionDynamic function = ChatFunctionDynamic.builder()
                .name("get_weather")
                .description("Get the current weather of a location")
                .addProperty(ChatFunctionProperty.builder()
                        .name("location")
                        .type("string")
                        .description("City and state, for example: León, Guanajuato")
                        .build())
                .addProperty(ChatFunctionProperty.builder()
                        .name("unit")
                        .type("string")
                        .description("The temperature unit, can be 'celsius' or 'fahrenheit'")
                        .enumValues(new HashSet<>(Arrays.asList("celsius", "fahrenheit")))
                        .required(true)
                        .build())
                .build();

        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), "You are a helpful assistant.");
        final ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), "What is the weather in Monterrey, Nuevo León?");
        messages.add(systemMessage);
        messages.add(userMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo-0613")
                .messages(messages)
                .functions(Collections.singletonList(function))
                .n(1)
                .maxTokens(100)
                .logitBias(new HashMap<>())
                .build();

        ChatCompletionChoice choice = service.createChatCompletion(chatCompletionRequest).getChoices().get(0);
        assertEquals("function_call", choice.getFinishReason());
        assertNotNull(choice.getMessage().getFunctionCall());
        assertEquals("get_weather", choice.getMessage().getFunctionCall().getName());
        assertInstanceOf(ObjectNode.class, choice.getMessage().getFunctionCall().getArguments());
        assertNotNull(choice.getMessage().getFunctionCall().getArguments().get("location"));
        assertNotNull(choice.getMessage().getFunctionCall().getArguments().get("unit"));
    }

    @Test
    void streamChatCompletionWithFunctions() {
        final List<ChatFunction> functions = Collections.singletonList(ChatFunction.builder()
                .name("get_weather")
                .description("Get the current weather in a given location")
                .executor(Weather.class, w -> new WeatherResponse(w.location, w.unit, 25, "sunny"))
                .build());
        final FunctionExecutor functionExecutor = new FunctionExecutor(functions);

        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), "You are a helpful assistant.");
        final ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), "What is the weather in Monterrey, Nuevo León?");
        messages.add(systemMessage);
        messages.add(userMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo-0613")
                .messages(messages)
                .functions(functionExecutor.getFunctions())
                .n(1)
                .maxTokens(100)
                .logitBias(new HashMap<>())
                .build();

        ChatMessage accumulatedMessage = service.mapStreamToAccumulator(service.streamChatCompletion(chatCompletionRequest))
                .blockingLast()
                .getAccumulatedMessage();
        assertNotNull(accumulatedMessage.getFunctionCall());
        assertEquals("get_weather", accumulatedMessage.getFunctionCall().getName());
        assertInstanceOf(ObjectNode.class, accumulatedMessage.getFunctionCall().getArguments());

        ChatMessage callResponse = functionExecutor.executeAndConvertToMessageHandlingExceptions(accumulatedMessage.getFunctionCall());
        assertNotEquals("error", callResponse.getName());

        // this performs an unchecked cast
        WeatherResponse functionExecutionResponse = functionExecutor.execute(accumulatedMessage.getFunctionCall());
        assertInstanceOf(WeatherResponse.class, functionExecutionResponse);
        assertEquals(25, functionExecutionResponse.temperature);

        JsonNode jsonFunctionExecutionResponse = functionExecutor.executeAndConvertToJson(accumulatedMessage.getFunctionCall());
        assertInstanceOf(ObjectNode.class, jsonFunctionExecutionResponse);
        assertEquals("25", jsonFunctionExecutionResponse.get("temperature").asText());


        messages.add(accumulatedMessage);
        messages.add(callResponse);

        ChatCompletionRequest chatCompletionRequest2 = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo-0613")
                .messages(messages)
                .functions(functionExecutor.getFunctions())
                .n(1)
                .maxTokens(100)
                .logitBias(new HashMap<>())
                .build();

        ChatMessage accumulatedMessage2 = service.mapStreamToAccumulator(service.streamChatCompletion(chatCompletionRequest2))
                .blockingLast()
                .getAccumulatedMessage();
        assertNull(accumulatedMessage2.getFunctionCall());
        assertNotNull(accumulatedMessage2.getContent());
    }

    @Test
    void streamChatCompletionWithDynamicFunctions() {
        ChatFunctionDynamic function = ChatFunctionDynamic.builder()
                .name("get_weather")
                .description("Get the current weather of a location")
                .addProperty(ChatFunctionProperty.builder()
                        .name("location")
                        .type("string")
                        .description("City and state, for example: León, Guanajuato")
                        .build())
                .addProperty(ChatFunctionProperty.builder()
                        .name("unit")
                        .type("string")
                        .description("The temperature unit, can be 'celsius' or 'fahrenheit'")
                        .enumValues(new HashSet<>(Arrays.asList("celsius", "fahrenheit")))
                        .required(true)
                        .build())
                .build();

        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), "You are a helpful assistant.");
        final ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), "What is the weather in Monterrey, Nuevo León?");
        messages.add(systemMessage);
        messages.add(userMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo-0613")
                .messages(messages)
                .functions(Collections.singletonList(function))
                .n(1)
                .maxTokens(100)
                .logitBias(new HashMap<>())
                .build();

        ChatMessage accumulatedMessage = service.mapStreamToAccumulator(service.streamChatCompletion(chatCompletionRequest))
                .blockingLast()
                .getAccumulatedMessage();
        assertNotNull(accumulatedMessage.getFunctionCall());
        assertEquals("get_weather", accumulatedMessage.getFunctionCall().getName());
        assertInstanceOf(ObjectNode.class, accumulatedMessage.getFunctionCall().getArguments());
        assertNotNull(accumulatedMessage.getFunctionCall().getArguments().get("location"));
        assertNotNull(accumulatedMessage.getFunctionCall().getArguments().get("unit"));
    }

    @Test
    void createChatCompletionWithToolFunctions() {

        final List<ChatFunction> functions = Collections.singletonList(ChatFunction.builder()
                .name("get_weather")
                .description("Get the current weather in a given location")
                .executor(Weather.class, w -> new WeatherResponse(w.location, w.unit, 25, "sunny"))
                .build());
        final FunctionExecutor functionExecutor = new FunctionExecutor(functions);
        final ChatTool tool = new ChatTool();
        tool.setFunction(functionExecutor.getFunctions().get(0));
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), "You are a helpful assistant.");
        final ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), "What is the weather in Monterrey, Nuevo León?");
        messages.add(systemMessage);
        messages.add(userMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo-0613")
                .messages(messages)
                .tools(Arrays.asList(tool))
                .toolChoice("auto")
                .n(1)
                .maxTokens(100)
                .logitBias(new HashMap<>())
                .build();

        ChatCompletionChoice choice = service.createChatCompletion(chatCompletionRequest).getChoices().get(0);
        assertEquals("tool_calls", choice.getFinishReason());

        assertEquals("get_weather", choice.getMessage().getToolCalls().get(0).getFunction().getName());
        assertInstanceOf(ObjectNode.class, choice.getMessage().getToolCalls().get(0).getFunction().getArguments());

        ChatMessage callResponse = functionExecutor.executeAndConvertToMessageHandlingExceptions(choice.getMessage().getToolCalls().get(0).getFunction());
        assertNotEquals("error", callResponse.getName());

        // this performs an unchecked cast
        WeatherResponse functionExecutionResponse = functionExecutor.execute(choice.getMessage().getToolCalls().get(0).getFunction());
        assertInstanceOf(WeatherResponse.class, functionExecutionResponse);
        assertEquals(25, functionExecutionResponse.temperature);

        JsonNode jsonFunctionExecutionResponse = functionExecutor.executeAndConvertToJson(choice.getMessage().getToolCalls().get(0).getFunction());
        assertInstanceOf(ObjectNode.class, jsonFunctionExecutionResponse);
        assertEquals("25", jsonFunctionExecutionResponse.get("temperature").asText());

        //Construct message for tool_calls
        ChatMessageTool chatMessageTool = new ChatMessageTool(choice.getMessage().getToolCalls().get(0).getId(),
                ChatMessageRole.TOOL.value(),
                jsonFunctionExecutionResponse.toString(),
                choice.getMessage().getToolCalls().get(0).getFunction().getName());

        messages.add(choice.getMessage());
        messages.add(chatMessageTool);

        ChatCompletionRequest chatCompletionRequest2 = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo-0613")
                .messages(messages)
                .tools(Arrays.asList(tool))
                .toolChoice("auto")
                .n(1)
                .maxTokens(100)
                .logitBias(new HashMap<>())
                .build();

        ChatCompletionChoice choice2 = service.createChatCompletion(chatCompletionRequest2).getChoices().get(0);
        assertNotEquals("tool_calls", choice2.getFinishReason()); // could be stop or length, but should not be function_call
        assertNull(choice2.getMessage().getFunctionCall());
        assertNotNull(choice2.getMessage().getContent());
    }

    @Test
    void createChatCompletionWithMultipleToolCalls() {
        final List<ChatFunction> functions = Arrays.asList(ChatFunction.builder()
                        .name("get_weather")
                        .description("Get the current weather in a given location")
                        .executor(Weather.class, w -> {
                            switch (w.location) {
                                case "tokyo":
                                    return new WeatherResponse(w.location, w.unit, 10, "cloudy");
                                case "san francisco":
                                    return new WeatherResponse(w.location, w.unit, 72, "sunny");
                                case "paris":
                                    return new WeatherResponse(w.location, w.unit, 22, "sunny");
                                default:
                                    return new WeatherResponse(w.location, w.unit, 0, "unknown");
                            }
                        }).build(),
                ChatFunction.builder().name("getCities").description("Get a list of cities by time").executor(City.class, v -> {
                    assertEquals("2022-12-01", v.time);
                    return Arrays.asList("tokyo", "paris");
                }).build()
        );
        final FunctionExecutor functionExecutor = new FunctionExecutor(functions);

        List<ChatTool> tools = new ArrayList<>();
        tools.add(new ChatTool<>(functions.get(0)));
        tools.add(new ChatTool<>(functions.get(1)));

        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), "You are a helpful assistant.");
        final ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), "What is the weather like in cities with weather on 2022-12-01 ?");
        messages.add(systemMessage);
        messages.add(userMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo-0613")
                .messages(messages)
                .tools(tools)
                .toolChoice("auto")
                .n(1)
                .maxTokens(100)
                .logitBias(new HashMap<>())
                .build();

        ChatCompletionChoice choice = service.createChatCompletion(chatCompletionRequest).getChoices().get(0);
        assertEquals("tool_calls", choice.getFinishReason());
        assertEquals(1, choice.getMessage().getToolCalls().size());
        assertEquals("getCities", choice.getMessage().getToolCalls().get(0).getFunction().getName());
        assertInstanceOf(ObjectNode.class, choice.getMessage().getToolCalls().get(0).getFunction().getArguments());

        ChatMessage callResponse = functionExecutor.executeAndConvertToMessageHandlingExceptions(choice.getMessage().getToolCalls().get(0).getFunction());
        assertNotEquals("error", callResponse.getName());

        Object execute = functionExecutor.execute(choice.getMessage().getToolCalls().get(0).getFunction());
        assertInstanceOf(List.class, execute);
        List<String> cities = (List<String>) execute;

        // List<ChatMessageTool> chatMessageTools = new ArrayList<>();
        ChatMessageTool cityCallResultMsg = new ChatMessageTool(choice.getMessage().getToolCalls().get(0).getId(),
                ChatMessageRole.TOOL.value(), "{\"cities\": " + cities + "}",
                choice.getMessage().getToolCalls().get(0).getFunction().getName());

        messages.add(choice.getMessage());
        messages.add(cityCallResultMsg);

        ChatCompletionRequest chatCompletionRequest2 = ChatCompletionRequest
                .builder()
                //3.5 there may be logical issues
                .model("gpt-3.5-turbo-0125")
                .messages(messages)
                .tools(tools)
                .toolChoice("auto")
                .n(1)
                .maxTokens(100)
                .logitBias(new HashMap<>())
                .build();

        ChatCompletionChoice choice2 = service.createChatCompletion(chatCompletionRequest2).getChoices().get(0);
        assertEquals("tool_calls", choice2.getFinishReason());
        assertEquals(2, choice2.getMessage().getToolCalls().size());
        assertEquals("get_weather", choice2.getMessage().getToolCalls().get(0).getFunction().getName());
        assertEquals("get_weather", choice2.getMessage().getToolCalls().get(1).getFunction().getName());
        assertInstanceOf(ObjectNode.class, choice2.getMessage().getToolCalls().get(0).getFunction().getArguments());
        assertInstanceOf(ObjectNode.class, choice2.getMessage().getToolCalls().get(1).getFunction().getArguments());

        List<ChatMessageTool> cityWeatherCallResultMsgs = new ArrayList<>();
        for (ChatToolCalls weatherToolCall : choice2.getMessage().getToolCalls()) {
            Object itemResult = functionExecutor.execute(weatherToolCall.getFunction());
            assertInstanceOf(WeatherResponse.class, itemResult);
            WeatherResponse weatherResponse = (WeatherResponse) itemResult;
            cityWeatherCallResultMsgs.add(new ChatMessageTool(weatherToolCall.getId(),
                    ChatMessageRole.TOOL.value(), weatherResponse.toString(),
                    weatherToolCall.getFunction().getName()));
        }

        messages.add(choice2.getMessage());
        for (ChatMessageTool cityWeatherCallResultMsg : cityWeatherCallResultMsgs) {
            messages.add(cityWeatherCallResultMsg);
        }

        ChatCompletionRequest chatCompletionRequest3 = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo-0613")
                .messages(messages)
                .tools(tools)
                .toolChoice("auto")
                .n(1)
                .maxTokens(100)
                .logitBias(new HashMap<>())
                .build();

        ChatCompletionChoice choice3 = service.createChatCompletion(chatCompletionRequest3).getChoices().get(0);
        assertNotEquals("tool_calls", choice3.getFinishReason());
        assertNull(choice3.getMessage().getFunctionCall());
        assertNotNull(choice3.getMessage().getContent());
    }

}
