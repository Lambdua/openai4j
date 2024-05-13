package example;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.function.FunctionDefinition;
import com.theokanning.openai.function.FunctionExecutorManager;
import com.theokanning.openai.service.OpenAiService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author LiangTao
 * @date 2024年05月13 11:28
 **/
public class ComplexFunctionDefineUseExample {
    public static class ComponentSearch {

        @JsonSchemaDescription("The logical relationship of query conditions")
        @JsonProperty(required = true)
        public MergeType mergeType;

        @JsonSchemaDescription("query criteria list collection")
        @JsonProperty(required = true)
        public List<QueryCondition> queryConditionList;


        public static class QueryCondition {
            @JsonSchemaDescription("the name of the queried attribute")
            @JsonSchemaDefault("family and type")
            @JsonProperty(required = true)
            public String propertyName;

            @JsonSchemaDescription("comparator")
            @JsonProperty(required = true)
            public Operate operate;

            @JsonSchemaDescription("attribute value")
            @JsonProperty(required = true)
            public String value;


            public enum Operate {
                EQUAL,
                NOT_EQUAL,
                GREATER_THAN,
                LESS_THAN,
                GREATER_THAN_OR_EQUAL,
                LESS_THAN_OR_EQUAL,
                CONTAIN,
                NOT_CONTAIN;
            }
        }

        public enum MergeType {
            AND,
            OR;
        }

    }

    public static void main(String[] args) {
        // complexFunctionExample();
        noParameterFunctionExample();
    }

    private static void noParameterFunctionExample() {
        OpenAiService openAiService = new OpenAiService();
        FunctionDefinition fd = FunctionDefinition.builder()
                .name("get_current_time")
                .description("Get the current time")
                .executor(c -> System.currentTimeMillis())
                .build();
        FunctionExecutorManager functionExecutorManager = new FunctionExecutorManager();
        functionExecutorManager.addFunctionDefinition(fd);

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new SystemMessage("You are responsible for assisting users in solving problems, and you can complete tasks based on the different abilities provided to you\n" +
                "Your answer is limited to the following abilities. You can choose the appropriate ability to answer the user's question based on their input\n" +
                "# your abilities:\n" +
                "- Get the current time:`get_current_time`\n" +
                "\n"));

        messages.add(new UserMessage("What is the current time?"));

        ChatCompletionResult chatCompletion = openAiService.createChatCompletion(ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .tools(Arrays.asList(new ChatTool(fd)))
                .temperature(1D)
                .build());
        ChatCompletionChoice choice = chatCompletion.getChoices().get(0);
        System.out.println(choice.getFinishReason());
        List<ChatToolCall> toolCalls = choice.getMessage().getToolCalls();
        ChatToolCall chatToolCall = toolCalls.get(0);
        ToolMessage toolMessage = functionExecutorManager.executeAndConvertToChatMessage(chatToolCall.getFunction().getName(), chatToolCall.getFunction().getArguments(), chatToolCall.getId());
        System.out.println(toolMessage);

    }

    private static void complexFunctionExample() {
        OpenAiService openAiService = new OpenAiService();
        FunctionDefinition fd = FunctionDefinition.<ComponentSearch>builder()
                .name("get_component_set")
                .description("Obtain the component set information of the model based on the query conditions provided by the user")
                .parametersDefinitionByClass(ComponentSearch.class)
                .executor(c -> c)
                .build();
        FunctionExecutorManager functionExecutorManager = new FunctionExecutorManager();
        functionExecutorManager.addFunctionDefinition(fd);

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new SystemMessage(
                "You are responsible for assisting users in solving problems, and you can complete tasks based on the different abilities provided to you\n" +
                        "Your answer is limited to the following abilities. You can choose the appropriate ability to answer the user's question based on their input\n" +
                        "# your abilities:\n" +
                        "- Based on the component query language provided by the user, analyze and extract the query conditions to obtain the component set information of the model:`get_component_set`\n" +
                        "\n"));
        messages.add(new UserMessage("When calling component queries, you should select the most suitable attribute name class from this collection for component queries," +
                "This is a collection of component attribute names for the model: [\"A1\",\"b\",\"B1\",\"b1\",\"b2\",\"h\",\"H1\",\"h1\",\"h2\",\"h3\",\"h4\",\"name\",\",\"seismicGrade\",\"visibleLightTransmittance\",\"width\",\"widthCoefficient\"," +
                "\"familyAndType]"));

        messages.add(new UserMessage("Search for components with a width of 12 and a name of hot return water pipe"));

        ChatCompletionResult chatCompletion = openAiService.createChatCompletion(ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .tools(Arrays.asList(new ChatTool(fd)))
                .temperature(1D)
                .build());
        ChatCompletionChoice choice = chatCompletion.getChoices().get(0);
        System.out.println(choice.getFinishReason());
        List<ChatToolCall> toolCalls = choice.getMessage().getToolCalls();
        ChatToolCall chatToolCall = toolCalls.get(0);
        ToolMessage toolMessage = functionExecutorManager.executeAndConvertToChatMessage(chatToolCall.getFunction().getName(), chatToolCall.getFunction().getArguments(), chatToolCall.getId());
        System.out.println(toolMessage);
    }
}
