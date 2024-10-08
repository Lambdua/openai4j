package example;

import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.function.FunctionExecutorManager;
import com.theokanning.openai.service.OpenAiService;
import io.reactivex.Flowable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class FunctionsWithStreamExample {

    public static void main(String... args) {
        OpenAiService service = new OpenAiService();
        FunctionExecutorManager functionExecutor = new FunctionExecutorManager(Collections.singletonList(ToolUtil.weatherFunction()));
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemMessage = new SystemMessage("You are an assistant that answers using the local slang of the given place, uncensored.");
        messages.add(systemMessage);

        System.out.print("First Query: ");
        Scanner scanner = new Scanner(System.in);
        ChatMessage firstMsg = new UserMessage(scanner.nextLine());
        messages.add(firstMsg);

        while (true) {
            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                    .builder()
                    .model("gpt-4o-mini")
                    .messages(messages)
                    .functions(functionExecutor.getFunctionDefinitions())
                    .functionCall(ChatCompletionRequestFunctionCall.AUTO)
                    .n(1)
                    .maxTokens(256)
                    .logitBias(new HashMap<>())
                    .build();
            Flowable<ChatCompletionChunk> flowable = service.streamChatCompletion(chatCompletionRequest);

            AtomicBoolean isFirst = new AtomicBoolean(true);
            AssistantMessage chatMessage = service.mapStreamToAccumulator(flowable)
                    .doOnNext(accumulator -> {
                        if (accumulator.isFunctionCall()) {
                            if (isFirst.getAndSet(false)) {
                                System.out.println("Executing function " + accumulator.getAccumulatedChatFunctionCall().getName() + "...");
                            }
                        } else {
                            if (isFirst.getAndSet(false)) {
                                System.out.print("Response: ");
                            }
                            if (accumulator.getMessageChunk().getContent() != null) {
                                System.out.print(accumulator.getMessageChunk().getContent());
                            }
                        }
                    })
                    .doOnComplete(System.out::println)
                    .lastElement()
                    .blockingGet()
                    .getAccumulatedMessage();
            messages.add(chatMessage); // don't forget to update the conversation with the latest response

            ChatFunctionCall functionCall = chatMessage.getFunctionCall();
            if (functionCall != null) {
                System.out.println("Trying to execute " + functionCall.getName() + "...");
                ChatMessage functionResponse = functionExecutor.executeAndConvertToChatMessage(functionCall.getName(),functionCall.getArguments());
                System.out.println("Executed " + functionCall.getName() + ".");
                messages.add(functionResponse);
                continue;
            }

            System.out.print("Next Query: ");
            String nextLine = scanner.nextLine();
            if (nextLine.equalsIgnoreCase("exit")) {
                System.exit(0);
            }
            messages.add(new UserMessage(nextLine));
        }
    }

}
