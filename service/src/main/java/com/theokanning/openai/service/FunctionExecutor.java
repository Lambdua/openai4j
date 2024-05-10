package com.theokanning.openai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.theokanning.openai.assistants.run.ToolCallFunction;
import com.theokanning.openai.completion.chat.ChatFunction;
import com.theokanning.openai.completion.chat.ChatFunctionCall;
import com.theokanning.openai.completion.chat.FunctionMessage;
import com.theokanning.openai.completion.chat.ToolMessage;

import java.util.*;

/**
 * @deprecated This class is deprecated and will be removed in a future version
 * replaced by {@link com.theokanning.openai.function.FunctionExecutorManager}
 */
@Deprecated
public class FunctionExecutor {

    private ObjectMapper MAPPER = new ObjectMapper();
    private final Map<String, ChatFunction> FUNCTIONS = new HashMap<>();

    public FunctionExecutor(List<ChatFunction> functions) {
        setFunctions(functions);
    }

    public FunctionExecutor(List<ChatFunction> functions, ObjectMapper objectMapper) {
        setFunctions(functions);
        setObjectMapper(objectMapper);
    }

    /**
     * @deprecated Use {@link #executeAndConvertToMessageHandlingExceptions(ChatFunctionCall)} instead
     */
    @Deprecated
    public Optional<FunctionMessage> executeAndConvertToMessageSafely(ChatFunctionCall call) {
        try {
            return Optional.ofNullable(executeAndConvertToMessage(call));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public Optional<ToolMessage> executeAndConvertToMessageSafely(ChatFunctionCall call, String toolId) {
        try {
            return Optional.ofNullable(executeAndConvertToMessage(call, toolId));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    /**
     * @author liangtao
     * @date 2024/4/12
     * @deprecated Use {@link #executeAndConvertToMessageHandlingExceptions(ChatFunctionCall)} instead
     **/
    @Deprecated
    public FunctionMessage executeAndConvertToMessageHandlingExceptions(ChatFunctionCall call) {
        try {
            return executeAndConvertToMessage(call);
        } catch (Exception exception) {
            exception.printStackTrace();
            return convertExceptionToMessage(exception);
        }
    }

    public ToolMessage executeAndConvertToMessageHandlingExceptions(ChatFunctionCall call, String toolId) {
        try {
            return executeAndConvertToMessage(call, toolId);
        } catch (Exception exception) {
            exception.printStackTrace();
            return convertExceptionToMessage(exception, toolId);
        }
    }

    /**
     * @deprecated Use {@link #convertExceptionToMessage(Exception, String)} instead
     */
    @Deprecated
    public FunctionMessage convertExceptionToMessage(Exception exception) {
        String error = exception.getMessage() == null ? exception.toString() : exception.getMessage();
        return new FunctionMessage("{\"error\": \"" + error + "\"}", "error");
    }

    public ToolMessage convertExceptionToMessage(Exception exception, String toolId) {
        String error = exception.getMessage() == null ? exception.toString() : exception.getMessage();
        return new ToolMessage("{\"error\": \"" + error + "\"}", toolId);
    }


    /**
     * @deprecated Use {@link #executeAndConvertToMessage(ChatFunctionCall, String)} instead
     */
    @Deprecated
    public FunctionMessage executeAndConvertToMessage(ChatFunctionCall call) {
        return new FunctionMessage(executeAndConvertToJson(call).toPrettyString(), call.getName());
    }

    public ToolMessage executeAndConvertToMessage(ChatFunctionCall call, String toolId) {
        return new ToolMessage(executeAndConvertToJson(call).toPrettyString(), toolId);
    }

    /**
     * 将assistant-v2 api中的需要执行的function执行并返回json
     *
     * @param call
     * @return
     */
    public JsonNode executeAndConvertToJson(ToolCallFunction call) {
        return executeAndConvertToJson(call.getName(), call.getArguments());
    }

    public JsonNode executeAndConvertToJson(ChatFunctionCall call) {
        return executeAndConvertToJson(call.getName(), call.getArguments());
    }

    public <T> T execute(ChatFunctionCall functionCall) {
        return execute(functionCall.getName(), functionCall.getArguments());
    }

    public JsonNode executeAndConvertToJson(String funName, JsonNode arguments) {
        try {
            Object execution = execute(funName, arguments);
            if (execution instanceof TextNode) {
                JsonNode objectNode = MAPPER.readTree(((TextNode) execution).asText());
                if (objectNode.isMissingNode())
                    return (JsonNode) execution;
                return objectNode;
            }
            if (execution instanceof ObjectNode) {
                return (JsonNode) execution;
            }
            if (execution instanceof String) {
                JsonNode objectNode = MAPPER.readTree((String) execution);
                if (objectNode.isMissingNode())
                    throw new RuntimeException("Parsing exception");
                return objectNode;
            }
            return MAPPER.readValue(MAPPER.writeValueAsString(execution), JsonNode.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    @SuppressWarnings("unchecked")
    public <T> T execute(String funName, JsonNode arguments) {
        ChatFunction function = FUNCTIONS.get(funName);
        Object obj;
        try {
            obj = MAPPER.readValue(arguments instanceof TextNode ? arguments.asText() : arguments.toPrettyString(), function.getParametersClass());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return (T) function.getExecutor().apply(obj);
    }

    public List<ChatFunction> getFunctions() {
        return new ArrayList<>(FUNCTIONS.values());
    }

    public void setFunctions(List<ChatFunction> functions) {
        this.FUNCTIONS.clear();
        functions.forEach(f -> this.FUNCTIONS.put(f.getName(), f));
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.MAPPER = objectMapper;
    }

}
