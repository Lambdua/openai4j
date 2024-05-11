package example;

import com.theokanning.openai.assistants.run.ToolChoice;
import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.function.FunctionDefinition;
import com.theokanning.openai.function.FunctionExecutorManager;
import com.theokanning.openai.service.OpenAiService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author LiangTao
 * @date 2024年04月30 11:24
 **/
public class ChatExample {

    public static void main(String[] args) {
        // simpleChat();
        // gptVision();
        // toolChat();
        // functionChat();
        streamChatWithTool();
    }

    static void simpleChat() {
        //api-key get from environment variable OPENAI_API_KEY
        OpenAiService service = new OpenAiService(Duration.ofSeconds(30));
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemMessage = new SystemMessage("You are a cute cat and will speak as such.");
        messages.add(systemMessage);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .n(1)
                .maxTokens(50)
                .build();
        ChatCompletionResult chatCompletion = service.createChatCompletion(chatCompletionRequest);
        System.out.println(chatCompletion.getChoices().get(0).getMessage().getContent());
    }

    static void functionChat() {
        OpenAiService service = new OpenAiService(Duration.ofSeconds(30));
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new SystemMessage("You are a helpful assistant.");
        final ChatMessage userMessage = new UserMessage("What is the weather in BeiJin?");
        messages.add(systemMessage);
        messages.add(userMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .functions(Collections.singletonList(ToolUtil.weatherFunction()))
                .functionCall("auto")
                .n(1)
                .maxTokens(100)
                .build();
        //Request is sent
        ChatCompletionChoice choice = service.createChatCompletion(chatCompletionRequest).getChoices().get(0);
        AssistantMessage functionCallMsg = choice.getMessage();
        ChatFunctionCall functionCall = functionCallMsg.getFunctionCall();
        System.out.println(functionCall);

        messages.add(functionCallMsg);
        messages.add(new FunctionMessage("the weather is fine today.", "get_weather"));

        //submit tool call
        ChatCompletionRequest toolCallRequest = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .n(1)
                .maxTokens(100)
                .build();
        ChatCompletionChoice toolCallChoice = service.createChatCompletion(toolCallRequest).getChoices().get(0);
        System.out.println(toolCallChoice.getMessage().getContent());
    }

    static void toolChat() {
        OpenAiService service = new OpenAiService(Duration.ofSeconds(30));
        final ChatTool tool = new ChatTool(ToolUtil.weatherFunction());
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new SystemMessage("You are a helpful assistant.");
        final ChatMessage userMessage = new UserMessage("What is the weather in BeiJin?");
        messages.add(systemMessage);
        messages.add(userMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                //Tools is a list; multiple tools can be included
                .tools(Collections.singletonList(tool))
                .toolChoice(ToolChoice.AUTO)
                .n(1)
                .maxTokens(100)
                .build();
        //Request is sent
        ChatCompletionChoice choice = service.createChatCompletion(chatCompletionRequest).getChoices().get(0);
        AssistantMessage toolCallMsg = choice.getMessage();
        ChatToolCall toolCall = toolCallMsg.getToolCalls().get(0);
        System.out.println(toolCall.getFunction());

        messages.add(toolCallMsg);
        messages.add(new ToolMessage("the weather is fine today.", toolCall.getId()));

        //submit tool call
        ChatCompletionRequest toolCallRequest = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .n(1)
                .maxTokens(100)
                .build();
        ChatCompletionChoice toolCallChoice = service.createChatCompletion(toolCallRequest).getChoices().get(0);
        System.out.println(toolCallChoice.getMessage().getContent());
    }

    static void streamChat() {
        //api-key get from environment variable OPENAI_API_KEY
        OpenAiService service = new OpenAiService(Duration.ofSeconds(30));
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemMessage = new SystemMessage("You are a cute cat and will speak as such.");
        messages.add(systemMessage);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .n(1)
                .maxTokens(50)
                .build();
        service.streamChatCompletion(chatCompletionRequest).blockingForEach(System.out::println);
        service.shutdownExecutor();
    }

    static void gptVision() {
        OpenAiService service = new OpenAiService(Duration.ofSeconds(20));
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new SystemMessage("You are a helpful assistant.");
        //Here, the imageMessage is intended for image recognition
        final ChatMessage imageMessage = UserMessage.buildImageMessage("What's in this image?",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg");
        messages.add(systemMessage);
        messages.add(imageMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model("gpt-4-turbo")
                .messages(messages)
                .n(1)
                .maxTokens(200)
                .build();
        ChatCompletionChoice choice = service.createChatCompletion(chatCompletionRequest).getChoices().get(0);
        System.out.println(choice.getMessage().getContent());
    }


    static void streamChatWithTool() {
        OpenAiService service = new OpenAiService(Duration.ofSeconds(30));
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
        FunctionExecutorManager toolExecutor = new FunctionExecutorManager(functions);

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

        ChatToolCall toolCall = toolCalls.get(0);
        ToolMessage toolMessage = toolExecutor.executeAndConvertToChatMessage(toolCall.getFunction().getName(),toolCall.getFunction().getArguments(), toolCall.getId());
        messages.add(accumulatedMessage);
        messages.add(toolMessage);

        System.out.println(accumulatedMessage.getContent());
        System.out.println(toolMessage.getContent());

        ChatCompletionRequest chatCompletionRequest2 = ChatCompletionRequest
                .builder()
                //3.5 there may be logical issues
                .model("gpt-3.5-turbo-0125")
                .messages(messages)
                .tools(tools)
                .toolChoice(ToolChoice.AUTO)
                .n(1)
                .maxTokens(100)
                .build();

        AssistantMessage accumulatedMessage2 = service.mapStreamToAccumulator(service.streamChatCompletion(chatCompletionRequest2))
                .blockingLast()
                .getAccumulatedMessage();

        System.out.println(accumulatedMessage2.getContent());
        messages.add(accumulatedMessage2);

        for (ChatToolCall weatherToolCall : accumulatedMessage2.getToolCalls()) {
            messages.add(toolExecutor.executeAndConvertToChatMessage(weatherToolCall.getFunction().getName(),weatherToolCall.getFunction().getArguments(), weatherToolCall.getId()));
        }

        ChatCompletionRequest chatCompletionRequest3 = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .tools(tools)
                .toolChoice(ToolChoice.AUTO)
                .n(1)
                .maxTokens(100)
                .build();

        AssistantMessage accumulatedMessage3 = service.mapStreamToAccumulator(service.streamChatCompletion(chatCompletionRequest3))
                .blockingLast()
                .getAccumulatedMessage();
        System.out.println(accumulatedMessage3.getContent());
    }

}
