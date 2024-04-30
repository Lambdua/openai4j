package com.theokanning.openai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.theokanning.openai.assistants.run.ToolChoice;
import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.util.ToolUtil;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ChatCompletionTest {

    OpenAiService service = new OpenAiService();

    @Test
    void createChatCompletion() {
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new SystemMessage("You are a dog and will speak as such.");
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
        final ChatMessage systemMessage = new SystemMessage("You are a dog and will speak as such.");
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
        assertTrue(!chunks.isEmpty());
        assertNotNull(chunks.get(1).getChoices().get(0));
    }

    @Test
    void createChatCompletionWithJsonMode() {
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new SystemMessage("You will generate a random name and return it in JSON format.");
        messages.add(systemMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .responseFormat(ChatResponseFormat.JSON_OBJECT)
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
        final List<ChatFunction> functions = Collections.singletonList(ToolUtil.weatherFunction());
        final FunctionExecutor functionExecutor = new FunctionExecutor(functions);

        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new SystemMessage("You are a helpful assistant.");
        final ChatMessage userMessage = new UserMessage("What is the weather in Monterrey, Nuevo León?");
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
        ToolUtil.WeatherResponse functionExecutionResponse = functionExecutor.execute(choice.getMessage().getFunctionCall());
        assertInstanceOf(ToolUtil.WeatherResponse.class, functionExecutionResponse);
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
        final ChatMessage systemMessage = new SystemMessage("You are a helpful assistant.");
        final ChatMessage userMessage = new UserMessage("What is the weather in Monterrey, Nuevo León?");
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
        final List<ChatFunction> functions = Collections.singletonList(ToolUtil.weatherFunction());
        final FunctionExecutor functionExecutor = new FunctionExecutor(functions);

        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new SystemMessage("You are a helpful assistant.");
        final ChatMessage userMessage = new UserMessage("What is the weather in Monterrey, Nuevo León?");
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

        AssistantMessage accumulatedMessage = service.mapStreamToAccumulator(service.streamChatCompletion(chatCompletionRequest))
                .blockingLast()
                .getAccumulatedMessage();
        assertNotNull(accumulatedMessage.getFunctionCall());
        assertEquals("get_weather", accumulatedMessage.getFunctionCall().getName());
        assertInstanceOf(ObjectNode.class, accumulatedMessage.getFunctionCall().getArguments());

        ChatMessage callResponse = functionExecutor.executeAndConvertToMessageHandlingExceptions(accumulatedMessage.getFunctionCall());
        assertNotEquals("error", callResponse.getName());

        // this performs an unchecked cast
        ToolUtil.WeatherResponse functionExecutionResponse = functionExecutor.execute(accumulatedMessage.getFunctionCall());
        assertInstanceOf(ToolUtil.WeatherResponse.class, functionExecutionResponse);
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

        AssistantMessage accumulatedMessage2 = service.mapStreamToAccumulator(service.streamChatCompletion(chatCompletionRequest2))
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
        final ChatMessage systemMessage = new SystemMessage("You are a helpful assistant.");
        final ChatMessage userMessage = new UserMessage("What is the weather in Monterrey, Nuevo León?");
        messages.add(systemMessage);
        messages.add(userMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .functions(Collections.singletonList(function))
                .n(1)
                .maxTokens(100)
                .logitBias(new HashMap<>())
                .build();

        AssistantMessage accumulatedMessage = service.mapStreamToAccumulator(service.streamChatCompletion(chatCompletionRequest))
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

        //声明一个工具,目前openai-tool只支持function类型的tool
        final ChatTool tool = new ChatTool(ToolUtil.weatherFunction());
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new SystemMessage("You are a helpful assistant.");
        final ChatMessage userMessage = new UserMessage("What is the weather in Monterrey, Nuevo León?");
        messages.add(systemMessage);
        messages.add(userMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                //这里的tools是一个list,可以传入多个tool
                .tools(Arrays.asList(tool))
                .toolChoice(ToolChoice.AUTO)
                .n(1)
                .maxTokens(100)
                .build();

        ChatCompletionChoice choice = service.createChatCompletion(chatCompletionRequest).getChoices().get(0);
        assertEquals("tool_calls", choice.getFinishReason());
        assertEquals("get_weather", choice.getMessage().getToolCalls().get(0).getFunction().getName());
        assertInstanceOf(ObjectNode.class, choice.getMessage().getToolCalls().get(0).getFunction().getArguments());

        ChatToolCall toolCall = choice.getMessage().getToolCalls().get(0);

        FunctionExecutor toolExecutor = new FunctionExecutor(Arrays.asList(ToolUtil.weatherFunction()));
        Object functionExecutionResponse = toolExecutor.execute(toolCall.getFunction());
        assertInstanceOf(ToolUtil.WeatherResponse.class, functionExecutionResponse);
        assertEquals(25, ((ToolUtil.WeatherResponse) functionExecutionResponse).temperature);

        JsonNode jsonFunctionExecutionResponse = toolExecutor.executeAndConvertToJson(toolCall.getFunction());
        assertInstanceOf(ObjectNode.class, jsonFunctionExecutionResponse);
        assertEquals("25", jsonFunctionExecutionResponse.get("temperature").asText());

        ToolMessage chatMessageTool = toolExecutor.executeAndConvertToMessageHandlingExceptions(toolCall.getFunction(), toolCall.getId());
        //确保不是异常的返回
        assertNotEquals("error", chatMessageTool.getName());

        messages.add(choice.getMessage());
        messages.add(chatMessageTool);

        ChatCompletionRequest chatCompletionRequest2 = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .tools(Arrays.asList(tool))
                .toolChoice(ToolChoice.AUTO)
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
                        .executor(ToolUtil.Weather.class, w -> {
                            switch (w.location) {
                                case "tokyo":
                                    return new ToolUtil.WeatherResponse(w.location, w.unit, 10, "cloudy");
                                case "san francisco":
                                    return new ToolUtil.WeatherResponse(w.location, w.unit, 72, "sunny");
                                case "paris":
                                    return new ToolUtil.WeatherResponse(w.location, w.unit, 22, "sunny");
                                default:
                                    return new ToolUtil.WeatherResponse(w.location, w.unit, 0, "unknown");
                            }
                        }).build(),
                ChatFunction.builder().name("getCities").description("Get a list of cities by time").executor(ToolUtil.City.class, v -> {
                    assertEquals("2022-12-01", v.time);
                    return Arrays.asList("tokyo", "paris");
                }).build()
        );
        final FunctionExecutor toolExecutor = new FunctionExecutor(functions);

        List<ChatTool> tools = new ArrayList<>();
        tools.add(new ChatTool(functions.get(0)));
        tools.add(new ChatTool(functions.get(1)));

        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new SystemMessage("You are a helpful assistant.");
        final ChatMessage userMessage = new UserMessage("What is the weather like in cities with weather on 2022-12-01 ?");
        messages.add(systemMessage);
        messages.add(userMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .tools(tools)
                .toolChoice(ToolChoice.AUTO)
                .n(1)
                .maxTokens(100)
                .logitBias(new HashMap<>())
                .build();

        ChatCompletionChoice choice = service.createChatCompletion(chatCompletionRequest).getChoices().get(0);
        assertEquals("tool_calls", choice.getFinishReason());
        //这里应该只有一个工具调用
        assertEquals(1, choice.getMessage().getToolCalls().size());
        assertEquals("getCities", choice.getMessage().getToolCalls().get(0).getFunction().getName());
        assertInstanceOf(ObjectNode.class, choice.getMessage().getToolCalls().get(0).getFunction().getArguments());

        ChatToolCall toolCall = choice.getMessage().getToolCalls().get(0);
        Object execute = toolExecutor.execute(toolCall.getFunction());
        assertInstanceOf(List.class, execute);


        JsonNode jsonNode = toolExecutor.executeAndConvertToJson(toolCall.getFunction());
        assertInstanceOf(ArrayNode.class, jsonNode);


        ToolMessage toolMessage = toolExecutor.executeAndConvertToMessageHandlingExceptions(toolCall.getFunction(), toolCall.getId());
        assertNotEquals("error", toolMessage.getName());

        messages.add(choice.getMessage());
        messages.add(toolMessage);

        ChatCompletionRequest chatCompletionRequest2 = ChatCompletionRequest
                .builder()
                //3.5 there may be logical issues
                .model("gpt-3.5-turbo-0125")
                .messages(messages)
                .tools(tools)
                .toolChoice(ToolChoice.AUTO)
                .n(1)
                .maxTokens(100)
                .logitBias(new HashMap<>())
                .build();

        ChatCompletionChoice choice2 = service.createChatCompletion(chatCompletionRequest2).getChoices().get(0);
        assertEquals("tool_calls", choice2.getFinishReason());
        //这里应该有两个工具调用
        assertEquals(2, choice2.getMessage().getToolCalls().size());
        assertEquals("get_weather", choice2.getMessage().getToolCalls().get(0).getFunction().getName());
        assertEquals("get_weather", choice2.getMessage().getToolCalls().get(1).getFunction().getName());
        assertInstanceOf(ObjectNode.class, choice2.getMessage().getToolCalls().get(0).getFunction().getArguments());
        assertInstanceOf(ObjectNode.class, choice2.getMessage().getToolCalls().get(1).getFunction().getArguments());

        messages.add(choice2.getMessage());

        for (ChatToolCall weatherToolCall : choice2.getMessage().getToolCalls()) {
            Object itemResult = toolExecutor.execute(weatherToolCall.getFunction());
            assertInstanceOf(ToolUtil.WeatherResponse.class, itemResult);
            messages.add(toolExecutor.executeAndConvertToMessage(weatherToolCall.getFunction(), weatherToolCall.getId()));
        }

        ChatCompletionRequest chatCompletionRequest3 = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .tools(tools)
                .toolChoice(ToolChoice.AUTO)
                .n(1)
                .maxTokens(100)
                .logitBias(new HashMap<>())
                .build();

        ChatCompletionChoice choice3 = service.createChatCompletion(chatCompletionRequest3).getChoices().get(0);
        assertNotEquals("tool_calls", choice3.getFinishReason());
        assertNull(choice3.getMessage().getFunctionCall());
        assertNotNull(choice3.getMessage().getContent());
    }

    /**
     * 创建gpt4-turbo模型的图片识别
     *
     * @author liangtao
     * @date 2024/4/12
     **/
    @Test
    void createImageChatCompletion() {
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new SystemMessage("You are a helpful assistant.");
        final ChatMessage imageMessage = UserMessage.buildImageMessage("What'\''s in this image?",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg");
        messages.add(systemMessage);
        messages.add(imageMessage);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-4-turbo")
                .messages(messages)
                .n(1)
                .maxTokens(200)
                .build();

        ChatCompletionChoice choice = service.createChatCompletion(chatCompletionRequest).getChoices().get(0);
        assertNotNull(choice.getMessage().getContent());
    }


    /**
     * 流式请求中使用多个tool调用场景下的测试
     */
    @Test
    void streamChatMultipleToolCalls() {
        final List<ChatFunction> functions = Arrays.asList(
                //1. 天气查询
                ChatFunction.builder()
                        .name("get_weather")
                        .description("Get the current weather in a given location")
                        .executor(ToolUtil.Weather.class, w -> {
                            switch (w.location) {
                                case "tokyo":
                                    return new ToolUtil.WeatherResponse(w.location, w.unit, 10, "cloudy");
                                case "san francisco":
                                    return new ToolUtil.WeatherResponse(w.location, w.unit, 72, "sunny");
                                case "paris":
                                    return new ToolUtil.WeatherResponse(w.location, w.unit, 22, "sunny");
                                default:
                                    return new ToolUtil.WeatherResponse(w.location, w.unit, 0, "unknown");
                            }
                        }).build(),
                //2. 城市查询
                ChatFunction.builder().name("getCities").description("Get a list of cities by time").executor(ToolUtil.City.class, v -> Arrays.asList("tokyo", "paris")).build()
        );
        final FunctionExecutor toolExecutor = new FunctionExecutor(functions);

        List<ChatTool> tools = new ArrayList<>();
        tools.add(new ChatTool(functions.get(0)));
        tools.add(new ChatTool(functions.get(1)));

        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new SystemMessage("You are a helpful assistant.");
        final ChatMessage userMessage = new UserMessage("What is the weather like in cities with weather on 2022-12-01 ?");
        messages.add(systemMessage);
        messages.add(userMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .tools(tools)
                .toolChoice(ToolChoice.AUTO)
                .n(1)
                .maxTokens(200)
                .build();

        AssistantMessage accumulatedMessage = service.mapStreamToAccumulator(service.streamChatCompletion(chatCompletionRequest))
                .blockingLast()
                .getAccumulatedMessage();

        List<ChatToolCall> toolCalls = accumulatedMessage.getToolCalls();

        //这里应该只有一个工具调用
        assertEquals(1, toolCalls.size());
        assertEquals("getCities", toolCalls.get(0).getFunction().getName());
        assertInstanceOf(ObjectNode.class, toolCalls.get(0).getFunction().getArguments());

        ChatToolCall toolCall = toolCalls.get(0);
        Object execute = toolExecutor.execute(toolCall.getFunction());
        assertInstanceOf(List.class, execute);

        JsonNode jsonNode = toolExecutor.executeAndConvertToJson(toolCall.getFunction());
        assertInstanceOf(ArrayNode.class, jsonNode);


        ToolMessage toolMessage = toolExecutor.executeAndConvertToMessageHandlingExceptions(toolCall.getFunction(), toolCall.getId());
        assertNotEquals("error", toolMessage.getName());

        messages.add(accumulatedMessage);
        messages.add(toolMessage);

        ChatCompletionRequest chatCompletionRequest2 = ChatCompletionRequest
                .builder()
                //3.5 there may be logical issues
                .model("gpt-3.5-turbo-0125")
                .messages(messages)
                .tools(tools)
                .toolChoice(ToolChoice.AUTO)
                .n(1)
                .maxTokens(100)
                .logitBias(new HashMap<>())
                .build();

        // ChatCompletionChoice choice2 = service.createChatCompletion(chatCompletionRequest2).getChoices().get(0);
        AssistantMessage accumulatedMessage2 = service.mapStreamToAccumulator(service.streamChatCompletion(chatCompletionRequest2))
                .blockingLast()
                .getAccumulatedMessage();
        //这里应该有两个工具调用
        assertEquals(2, accumulatedMessage2.getToolCalls().size());
        assertEquals("get_weather", accumulatedMessage2.getToolCalls().get(0).getFunction().getName());
        assertEquals("get_weather", accumulatedMessage2.getToolCalls().get(1).getFunction().getName());
        assertInstanceOf(ObjectNode.class, accumulatedMessage2.getToolCalls().get(0).getFunction().getArguments());
        assertInstanceOf(ObjectNode.class, accumulatedMessage2.getToolCalls().get(1).getFunction().getArguments());
        messages.add(accumulatedMessage2);

        for (ChatToolCall weatherToolCall : accumulatedMessage2.getToolCalls()) {
            Object itemResult = toolExecutor.execute(weatherToolCall.getFunction());
            assertInstanceOf(ToolUtil.WeatherResponse.class, itemResult);
            messages.add(toolExecutor.executeAndConvertToMessage(weatherToolCall.getFunction(), weatherToolCall.getId()));
        }

        ChatCompletionRequest chatCompletionRequest3 = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .tools(tools)
                .toolChoice(ToolChoice.AUTO)
                .n(1)
                .maxTokens(100)
                .logitBias(new HashMap<>())
                .build();

        AssistantMessage accumulatedMessage3 = service.mapStreamToAccumulator(service.streamChatCompletion(chatCompletionRequest3))
                .blockingLast()
                .getAccumulatedMessage();
        assertNull(accumulatedMessage3.getToolCalls());
        assertNotNull(accumulatedMessage3.getContent());
    }


}
