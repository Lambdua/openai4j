package com.theokanning.openai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

import javax.validation.constraints.NotNull;

import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.utils.JsonUtil;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.theokanning.openai.assistants.run.ToolChoice;
import com.theokanning.openai.function.FunctionDefinition;
import com.theokanning.openai.function.FunctionExecutorManager;
import com.theokanning.openai.service.util.ToolUtil;

import lombok.Data;
import lombok.NoArgsConstructor;

class ChatCompletionTest {

    OpenAiService service = new OpenAiService(Duration.ofSeconds(20));

    @Test
    void createChatCompletion() {
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new SystemMessage("You are a dog and will speak as such.");
        messages.add(systemMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-4o-mini")
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
                .model("gpt-4o-mini")
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
    void streamOptionsChatCompletion() {
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new SystemMessage("You are a dog and will speak as such.");
        messages.add(systemMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-4o-mini")
                .messages(messages)
                .n(1)
                .maxTokens(50)
                .logitBias(new HashMap<>())
                .stream(true)
                .streamOptions(StreamOption.INCLUDE)
                .build();

        List<ChatCompletionChunk> chunks = new ArrayList<>();
        service.streamChatCompletion(chatCompletionRequest).blockingForEach(chunks::add);
        assertTrue(!chunks.isEmpty());
        assertNotNull(chunks.get(1).getChoices().get(0));
        assertNotNull(chunks.get(chunks.size() - 1).getUsage());
    }


    @Test
    void createChatCompletionWithJsonMode() {
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new SystemMessage("You will generate a random name and return it in JSON format.");
        messages.add(systemMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-4o-mini")
                .messages(messages)
                .responseFormat(ChatResponseFormat.JSON_OBJECT)
                .maxTokens(50)
                .logitBias(new HashMap<>())
                .build();

        ChatCompletionChoice choice = service.createChatCompletion(chatCompletionRequest).getChoices().get(0);
        assertTrue(JsonUtil.isValidJson(choice.getMessage().getContent()), "Response is not valid JSON");
    }


    @Test
    void createChatCompletionWithJsonSchema() throws JsonProcessingException {
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new SystemMessage("You are a helpful math tutor. Guide the user through the solution step by step.");
        final ChatMessage userMessage = new UserMessage("how can I solve 8x + 7 = -23");
        messages.add(systemMessage);
        messages.add(userMessage);

        Class<MathReasoning> rootClass = MathReasoning.class;
        ChatResponseFormat responseFormat = ChatResponseFormat.jsonSchema(ResponseJsonSchema.<MathReasoning>builder()
                        .name("math_reasoning")
                        .schemaClass(rootClass)
                        .strict(true)
                .build());

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-4o-2024-08-06")
                .messages(messages)
                .responseFormat(responseFormat)
                .maxTokens(1000)
                .build();

        ChatCompletionChoice choice = service.createChatCompletion(chatCompletionRequest).getChoices().get(0);
        MathReasoning mathReasoning = choice.getMessage().parsed(rootClass);

        String finalAnswer = mathReasoning.getFinal_answer();
        assertTrue(finalAnswer.contains("x"));
        assertTrue(finalAnswer.contains("="));
    }

    @Data
    @NoArgsConstructor
	private static class MathReasoning {
        @NotNull private List<Step> steps;
        @NotNull private String final_answer;
    }

    @Data
    @NoArgsConstructor
    private static class Step {
        @NotNull private String explanation;
        @NotNull private String output;
    }

    @Test
    void createChatCompletionWithFunctions() {
        final List<FunctionDefinition> functions = Collections.singletonList(ToolUtil.weatherFunction());
        // final FunctionExecutor functionExecutor = new FunctionExecutor(functions);
        final FunctionExecutorManager functionExecutor = new FunctionExecutorManager(functions);

        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new SystemMessage("You are a helpful assistant.");
        final ChatMessage userMessage = new UserMessage("What is the weather in Monterrey, Nuevo León?");
        messages.add(systemMessage);
        messages.add(userMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-4o-2024-08-06")
                .messages(messages)
                .functions(functions)
                .n(1)
                .maxTokens(100)
                .logitBias(new HashMap<>())
                .build();

        ChatCompletionChoice choice = service.createChatCompletion(chatCompletionRequest).getChoices().get(0);
        assertEquals("function_call", choice.getFinishReason());
        ChatFunctionCall functionCall = choice.getMessage().getFunctionCall();
        assertNotNull(functionCall);
        assertEquals("get_weather", functionCall.getName());
        assertInstanceOf(ObjectNode.class, functionCall.getArguments());

        ChatMessage callResponse = functionExecutor.executeAndConvertToChatMessage(functionCall.getName(), functionCall.getArguments());
        assertNotEquals("error", callResponse.getName());

        // this performs an unchecked cast
        ToolUtil.WeatherResponse functionExecutionResponse = functionExecutor.execute(functionCall.getName(), functionCall.getArguments());
        assertInstanceOf(ToolUtil.WeatherResponse.class, functionExecutionResponse);
        assertEquals(25, functionExecutionResponse.temperature);

        JsonNode jsonFunctionExecutionResponse = functionExecutor.executeAndConvertToJson(functionCall.getName(), functionCall.getArguments());
        assertInstanceOf(ObjectNode.class, jsonFunctionExecutionResponse);
        assertEquals("25", jsonFunctionExecutionResponse.get("temperature").asText());

        messages.add(choice.getMessage());
        messages.add(callResponse);

        ChatCompletionRequest chatCompletionRequest2 = ChatCompletionRequest
                .builder()
                .model("gpt-4o-2024-08-06")
                .messages(messages)
                .functions(functions)
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
                .model("gpt-4o-2024-08-06")
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
    void zeroArgStreamFunctionTest() {
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new SystemMessage("You are a helpful assistant.");
        final ChatMessage userMessage = new UserMessage("今天是几号?");
        messages.add(systemMessage);
        messages.add(userMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-4o-mini")
                .messages(messages)
                .functions(Collections.singletonList(FunctionDefinition.builder().name("get_today").description("Get the current date").executor((o) -> LocalDate.now()).build()))
                .n(1)
                .maxTokens(100)
                .logitBias(new HashMap<>())
                .build();
        AssistantMessage accumulatedMessage = service.mapStreamToAccumulator(service.streamChatCompletion(chatCompletionRequest))
                .blockingLast().getAccumulatedMessage();
        ChatFunctionCall functionCall = accumulatedMessage.getFunctionCall();
        assertNotNull(functionCall);
        assertEquals("get_today", functionCall.getName());
        assertInstanceOf(ObjectNode.class, functionCall.getArguments());
    }

    @Test
    void zeroArgStreamToolTest() {
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new SystemMessage("You are a helpful assistant.");
        final ChatMessage userMessage = new UserMessage("今天是几号?");
        messages.add(systemMessage);
        messages.add(userMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-4o-mini")
                .messages(messages)
                .tools(Collections.singletonList(
                        new ChatTool(FunctionDefinition.builder().name("get_today").description("Get the current date").executor((o) -> LocalDate.now()).build())
                ))
                .n(1)
                .maxTokens(100)
                .logitBias(new HashMap<>())
                .streamOptions(StreamOption.INCLUDE)
                .build();
        ChatMessageAccumulator chatMessageAccumulator = service.mapStreamToAccumulator(service.streamChatCompletion(chatCompletionRequest))
                .blockingLast();
        AssistantMessage accumulatedMessage =  chatMessageAccumulator.getAccumulatedMessage();
        List<ChatToolCall> toolCalls = accumulatedMessage.getToolCalls();
        assertNotNull(toolCalls);
        assertEquals(1, toolCalls.size());
        assertNotNull(chatMessageAccumulator.getUsage());
        ChatToolCall chatToolCall = toolCalls.get(0);
        ChatFunctionCall functionCall = chatToolCall.getFunction();
        assertEquals("get_today", functionCall.getName());
        assertInstanceOf(ObjectNode.class, functionCall.getArguments());
    }

    @Test
    void streamChatCompletionWithFunctions() {
        final List<FunctionDefinition> functions = Collections.singletonList(ToolUtil.weatherFunction());
        final FunctionExecutorManager functionExecutor = new FunctionExecutorManager(functions);

        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new SystemMessage("You are a helpful assistant.");
        final ChatMessage userMessage = new UserMessage("What is the weather in Monterrey, Nuevo León?");
        messages.add(systemMessage);
        messages.add(userMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-4o-2024-08-06")
                .messages(messages)
                .functions(functions)
                .n(1)
                .maxTokens(100)
                .logitBias(new HashMap<>())
                .build();

        AssistantMessage accumulatedMessage = service.mapStreamToAccumulator(service.streamChatCompletion(chatCompletionRequest))
                .blockingLast()
                .getAccumulatedMessage();
        ChatFunctionCall functionCall = accumulatedMessage.getFunctionCall();
        assertNotNull(functionCall);
        assertEquals("get_weather", functionCall.getName());
        assertInstanceOf(ObjectNode.class, functionCall.getArguments());

        ChatMessage callResponse = functionExecutor.executeAndConvertToChatMessage(functionCall.getName(), functionCall.getArguments());
        assertNotEquals("error", callResponse.getName());

        // this performs an unchecked cast
        ToolUtil.WeatherResponse functionExecutionResponse = functionExecutor.execute(functionCall.getName(), functionCall.getArguments());
        assertInstanceOf(ToolUtil.WeatherResponse.class, functionExecutionResponse);
        assertEquals(25, functionExecutionResponse.temperature);

        JsonNode jsonFunctionExecutionResponse = functionExecutor.executeAndConvertToJson(functionCall.getName(), functionCall.getArguments());
        assertInstanceOf(ObjectNode.class, jsonFunctionExecutionResponse);
        assertEquals("25", jsonFunctionExecutionResponse.get("temperature").asText());


        messages.add(accumulatedMessage);
        messages.add(callResponse);

        ChatCompletionRequest chatCompletionRequest2 = ChatCompletionRequest
                .builder()
                .model("gpt-4o-2024-08-06")
                .messages(messages)
                .functions(functions)
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
                .model("gpt-4o-mini")
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
                .model("gpt-4o-mini")
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

        FunctionExecutorManager toolExecutor = new FunctionExecutorManager(Arrays.asList(ToolUtil.weatherFunction()));
        ChatFunctionCall function = toolCall.getFunction();
        Object functionExecutionResponse = toolExecutor.execute(function.getName(), function.getArguments());
        assertInstanceOf(ToolUtil.WeatherResponse.class, functionExecutionResponse);
        assertEquals(25, ((ToolUtil.WeatherResponse) functionExecutionResponse).temperature);

        JsonNode jsonFunctionExecutionResponse = toolExecutor.executeAndConvertToJson(function.getName(), function.getArguments());
        assertInstanceOf(ObjectNode.class, jsonFunctionExecutionResponse);
        assertEquals("25", jsonFunctionExecutionResponse.get("temperature").asText());

        ToolMessage chatMessageTool = toolExecutor.executeAndConvertToChatMessage(function.getName(), function.getArguments(), toolCall.getId());
        //确保不是异常的返回
        assertNotEquals("error", chatMessageTool.getName());

        messages.add(choice.getMessage());
        messages.add(chatMessageTool);

        ChatCompletionRequest chatCompletionRequest2 = ChatCompletionRequest
                .builder()
                .model("gpt-4o-mini")
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
        final List<FunctionDefinition> functions = Arrays.asList(FunctionDefinition.<ToolUtil.Weather>builder()
                        .name("get_weather")
                        .description("Get the current weather in a given location")
                        .parametersDefinitionByClass(ToolUtil.Weather.class)
                        .executor(w -> {
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
                FunctionDefinition.<ToolUtil.City>builder().name("getCities").description("Get a list of cities by time")
                        .parametersDefinitionByClass(ToolUtil.City.class)
                        .executor(v -> {
                            assertEquals("2022-12-01", v.time);
                            return Arrays.asList("tokyo", "paris");
                        }).build()
        );
        final FunctionExecutorManager toolExecutor = new FunctionExecutorManager(functions);

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
                .model("gpt-4o-mini")
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
        ChatFunctionCall function = toolCall.getFunction();
        Object execute = toolExecutor.execute(function.getName(), function.getArguments());
        assertInstanceOf(List.class, execute);


        JsonNode jsonNode = toolExecutor.executeAndConvertToJson(function.getName(), function.getArguments());
        assertInstanceOf(ArrayNode.class, jsonNode);


        ToolMessage toolMessage = toolExecutor.executeAndConvertToChatMessage(function.getName(), function.getArguments(), toolCall.getId());
        assertNotEquals("error", toolMessage.getName());

        messages.add(choice.getMessage());
        messages.add(toolMessage);

        ChatCompletionRequest chatCompletionRequest2 = ChatCompletionRequest
                .builder()
                //3.5 there may be logical issues
                .model("gpt-4o-mini")
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
            Object itemResult = toolExecutor.execute(weatherToolCall.getFunction().getName(), weatherToolCall.getFunction().getArguments());
            assertInstanceOf(ToolUtil.WeatherResponse.class, itemResult);
            messages.add(toolExecutor.executeAndConvertToChatMessage(weatherToolCall.getFunction().getName(), weatherToolCall.getFunction().getArguments(), weatherToolCall.getId()));
        }

        ChatCompletionRequest chatCompletionRequest3 = ChatCompletionRequest
                .builder()
                .model("gpt-4o-mini")
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

    @Test
    void createLocalImageChatCompletion() throws URISyntaxException {
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new SystemMessage("You are a helpful assistant.");
        Path imagePath= Paths.get(Objects.requireNonNull(ChatCompletionTest.class.getClassLoader().getResource("vanter.jpg")).toURI());

        final ChatMessage imageMessage = UserMessage.buildImageMessage("What'\''s in this image?", imagePath);
        messages.add(systemMessage);
        messages.add(imageMessage);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-4o-mini")
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
        final List<FunctionDefinition> functions = Arrays.asList(
                //1. 天气查询
                FunctionDefinition.<ToolUtil.Weather>builder()
                        .name("get_weather")
                        .description("Get the current weather in a given location")
                        .parametersDefinitionByClass(ToolUtil.Weather.class)
                        .executor(w -> {
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
                FunctionDefinition.<ToolUtil.City>builder().name("getCities").description("Get a list of cities by time").parametersDefinitionByClass(ToolUtil.City.class).executor(v -> Arrays.asList("tokyo", "paris")).build()
        );
        final FunctionExecutorManager toolExecutor = new FunctionExecutorManager(functions);

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
                .model("gpt-4o-mini")
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
        ChatFunctionCall function = toolCall.getFunction();
        Object execute = toolExecutor.execute(function.getName(), function.getArguments());
        assertInstanceOf(List.class, execute);

        JsonNode jsonNode = toolExecutor.executeAndConvertToJson(function.getName(), function.getArguments());
        assertInstanceOf(ArrayNode.class, jsonNode);


        ToolMessage toolMessage = toolExecutor.executeAndConvertToChatMessage(function.getName(), function.getArguments(), toolCall.getId());
        assertNotEquals("error", toolMessage.getName());

        messages.add(accumulatedMessage);
        messages.add(toolMessage);

        ChatCompletionRequest chatCompletionRequest2 = ChatCompletionRequest
                .builder()
                //3.5 there may be logical issues
                .model("gpt-4o-mini")
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
            ChatFunctionCall call2 = weatherToolCall.getFunction();
            Object itemResult = toolExecutor.execute(call2.getName(), call2.getArguments());
            assertInstanceOf(ToolUtil.WeatherResponse.class, itemResult);
            messages.add(toolExecutor.executeAndConvertToChatMessage(call2.getName(), call2.getArguments(), weatherToolCall.getId()));
        }

        ChatCompletionRequest chatCompletionRequest3 = ChatCompletionRequest
                .builder()
                .model("gpt-4o-mini")
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


    @Test
    public void parallelToolCallTest() {
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new SystemMessage("You are a helpful assistant.");
        final ChatMessage userMessage = new UserMessage("What's the weather like in San Francisco, Tokyo, and Paris?");
        messages.add(systemMessage);
        messages.add(userMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .tools(Arrays.asList(new ChatTool(ToolUtil.weatherFunction())))
                .toolChoice(ToolChoice.AUTO)
                .parallelToolCalls(false)
                .n(1)
                .maxTokens(200)
                .build();

        AssistantMessage accumulatedMessage = service.mapStreamToAccumulator(service.streamChatCompletion(chatCompletionRequest))
                .blockingLast()
                .getAccumulatedMessage();

        List<ChatToolCall> toolCalls = accumulatedMessage.getToolCalls();
        assertEquals(1, toolCalls.size());
        assertEquals("get_weather", toolCalls.get(0).getFunction().getName());
        assertInstanceOf(ObjectNode.class, toolCalls.get(0).getFunction().getArguments());


        chatCompletionRequest.setParallelToolCalls(true);
        AssistantMessage accumulatedMessage2 = service.mapStreamToAccumulator(service.streamChatCompletion(chatCompletionRequest))
                .blockingLast()
                .getAccumulatedMessage();
        List<ChatToolCall> toolCalls2 = accumulatedMessage2.getToolCalls();
        assertEquals(3, toolCalls2.size());
        assertEquals("get_weather", toolCalls2.get(0).getFunction().getName());
    }

    /**
     * tool calling strict参数的使用对比
     *
     */
    @Test
    void toolCallingStrictTest(){
        String parmDefinition="{\n" +
                "    \"type\": \"object\",\n" +
                "    \"properties\": {\n" +
                "        \"table_name\": {\n" +
                "            \"type\": \"string\",\n" +
                "            \"enum\": [\n" +
                "                \"orders\"\n" +
                "            ]\n" +
                "        },\n" +
                "        \"columns\": {\n" +
                "            \"type\": \"array\",\n" +
                "            \"items\": {\n" +
                "                \"type\": \"string\",\n" +
                "                \"enum\": [\n" +
                "                    \"id\",\n" +
                "                    \"status\",\n" +
                "                    \"expected_delivery_date\",\n" +
                "                    \"delivered_at\",\n" +
                "                    \"shipped_at\",\n" +
                "                    \"ordered_at\",\n" +
                "                    \"canceled_at\"\n" +
                "                ]\n" +
                "            }\n" +
                "        },\n" +
                "        \"conditions\": {\n" +
                "            \"type\": \"array\",\n" +
                "            \"items\": {\n" +
                "                \"type\": \"object\",\n" +
                "                \"properties\": {\n" +
                "                    \"column\": {\n" +
                "                        \"type\": \"string\"\n" +
                "                    },\n" +
                "                    \"operator\": {\n" +
                "                        \"type\": \"string\",\n" +
                "                        \"enum\": [\n" +
                "                            \"=\",\n" +
                "                            \">\",\n" +
                "                            \"<\",\n" +
                "                            \">=\",\n" +
                "                            \"<=\",\n" +
                "                            \"!=\"\n" +
                "                        ]\n" +
                "                    },\n" +
                "                    \"value\": {\n" +
                "                        \"anyOf\": [\n" +
                "                            {\n" +
                "                                \"type\": \"string\"\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"type\": \"number\"\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"type\": \"object\",\n" +
                "                                \"properties\": {\n" +
                "                                    \"column_name\": {\n" +
                "                                        \"type\": \"string\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"required\": [\n" +
                "                                    \"column_name\"\n" +
                "                                ],\n" +
                "                                \"additionalProperties\": false\n" +
                "                            }\n" +
                "                        ]\n" +
                "                    }\n" +
                "                },\n" +
                "                \"required\": [\n" +
                "                    \"column\",\n" +
                "                    \"operator\",\n" +
                "                    \"value\"\n" +
                "                ],\n" +
                "                \"additionalProperties\": false\n" +
                "            }\n" +
                "        },\n" +
                "        \"order_by\": {\n" +
                "            \"type\": \"string\",\n" +
                "            \"enum\": [\n" +
                "                \"asc\",\n" +
                "                \"desc\"\n" +
                "            ]\n" +
                "        }\n" +
                "    },\n" +
                "    \"required\": [\n" +
                "        \"table_name\",\n" +
                "        \"columns\",\n" +
                "        \"conditions\",\n" +
                "        \"order_by\"\n" +
                "    ],\n" +
                "    \"additionalProperties\": false\n" +
                "}";
        SystemMessage systemMessage=new SystemMessage("You are a helpful assistant. The current date is August 6, 2024. You help users query for the data they are looking for by calling the query function.");
        UserMessage userMessage=new UserMessage("look up all my orders in may of last year that were fulfilled but not delivered on time");
        FunctionDefinition fd = FunctionDefinition.builder().name("query").description("Execute a query")
                .strict(true)
                .parametersDefinition(parmDefinition).build();
        ChatTool tool = new ChatTool(fd);
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(systemMessage);
        messages.add(userMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model("gpt-4o-mini")
                .messages(messages)
                .tools(Arrays.asList(tool))
                .toolChoice(ToolChoice.AUTO)
                .n(1)
                .build();
        AssistantMessage assistantMessage=service.createChatCompletion(chatCompletionRequest).getChoices().get(0).getMessage();
        assertNotNull(assistantMessage.getToolCalls());

        JsonNode arguments = assistantMessage.getToolCalls().get(0).getFunction().getArguments();
        assertNotNull(arguments);
        assertEquals("orders",arguments.get("table_name").asText());
        assertEquals("id",arguments.get("columns").get(0).asText());
        assertEquals("status",arguments.get("columns").get(1).asText());
        assertEquals("expected_delivery_date",arguments.get("columns").get(2).asText());
        assertEquals("delivered_at",arguments.get("columns").get(3).asText());
        assertEquals("shipped_at",arguments.get("columns").get(4).asText());
        assertEquals("ordered_at",arguments.get("columns").get(5).asText());
        assertEquals("canceled_at",arguments.get("columns").get(6).asText());
        assertEquals("asc",arguments.get("order_by").asText());
    }

}
