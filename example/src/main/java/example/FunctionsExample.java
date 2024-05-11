package example;

import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.function.FunctionDefinition;
import com.theokanning.openai.function.FunctionExecutorManager;
import com.theokanning.openai.service.OpenAiService;

import java.util.*;

class FunctionsExample {


    public static void main(String... args) {
        OpenAiService service = new OpenAiService();

        FunctionDefinition fd = ToolUtil.weatherFunction();
        FunctionExecutorManager functionExecutor = new FunctionExecutorManager(Collections.singletonList(fd));


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
                    .model("gpt-3.5-turbo-0613")
                    .messages(messages)
                    .functions(functionExecutor.getFunctionDefinitions())
                    .functionCall("auto")
                    .n(1)
                    .maxTokens(100)
                    .logitBias(new HashMap<>())
                    .build();
            AssistantMessage responseMessage = service.createChatCompletion(chatCompletionRequest).getChoices().get(0).getMessage();
            messages.add(responseMessage); // don't forget to update the conversation with the latest response

            ChatFunctionCall functionCall = responseMessage.getFunctionCall();
            if (functionCall != null) {
                System.out.println("Trying to execute " + functionCall.getName() + "...");
                FunctionMessage message = functionExecutor.executeAndConvertToChatMessage(functionCall.getName(), functionCall.getArguments());
                System.out.println("Executed " + functionCall.getName() + ".");
                messages.add(message);
            }

            System.out.println("Response: " + responseMessage.getContent());
            System.out.print("Next Query: ");
            String nextLine = scanner.nextLine();
            if (nextLine.equalsIgnoreCase("exit")) {
                System.exit(0);
            }
            messages.add(new UserMessage(nextLine));
        }
    }

}
