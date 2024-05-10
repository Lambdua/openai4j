package com.theokanning.openai.function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.assistants.run.SubmitToolOutputRequestItem;
import com.theokanning.openai.completion.chat.FunctionMessage;
import com.theokanning.openai.completion.chat.ToolMessage;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author LiangTao
 * @date 2024年05月10 13:30
 **/
public class FunctionExecutorManager {
    @Getter
    private final ObjectMapper mapper;
    private final Map<String, FunctionDefinition> functionHolderMap;

    private final ExecutorService executorService;

    public FunctionExecutorManager() {
        this(new ObjectMapper(), Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()), Collections.emptyList());
    }

    public FunctionExecutorManager(List<FunctionDefinition> functionDefinitionList) {
        this(new ObjectMapper(), Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()), functionDefinitionList);
    }

    public FunctionExecutorManager(ObjectMapper mapper, ExecutorService executorService) {
        this(mapper, executorService, Collections.emptyList());
    }

    public FunctionExecutorManager(ObjectMapper mapper, ExecutorService executorService, List<FunctionDefinition> functionDefinitionList) {
        this.functionHolderMap = new HashMap<>();
        for (FunctionDefinition functionDefinition : functionDefinitionList) {
            if (functionDefinition.getExecutor() == null) {
                throw new IllegalArgumentException("FunctionDefinition must have executor");
            }
            functionHolderMap.put(functionDefinition.getName(), functionDefinition);
        }
        this.mapper = mapper;
        this.executorService = executorService;
    }

    public FunctionDefinition getFunctionDefinition(String functionName) {
        return functionHolderMap.get(functionName);
    }

    public List<FunctionDefinition> getFunctionDefinitions() {
        return new ArrayList<>(functionHolderMap.values());
    }

    public void addFunctionDefinition(FunctionDefinition functionDefinition) {
        if (functionDefinition.getExecutor() == null) {
            throw new IllegalArgumentException("FunctionDefinition must have executor");
        }
        functionHolderMap.put(functionDefinition.getName(), functionDefinition);
    }

    public void clear() {
        functionHolderMap.clear();
    }

    public boolean remove(String functionName) {
        return functionHolderMap.remove(functionName) != null;
    }

    public boolean contains(String functionName) {
        return functionHolderMap.containsKey(functionName);
    }

    @SuppressWarnings("unchecked")
    public <T> T execute(String name, JsonNode arguments) {
        FunctionDefinition functionDefinition = functionHolderMap.get(name);
        if (functionDefinition == null) {
            throw new IllegalArgumentException("FunctionDefinition not found");
        }
        Object functionArg = functionDefinition.getParametersDefinitionClass() == null ? arguments : mapper.convertValue(arguments, functionDefinition.getParametersDefinitionClass());
        return (T) functionDefinition.getExecutor().apply(functionArg);
    }

    public <T> Future<T> executeAsync(String name, JsonNode arguments) {
        return executorService.submit(() -> execute(name, arguments));
    }

    public JsonNode executeAndConvertToJson(String funName, JsonNode arguments) {
        return mapper.convertValue(execute(funName, arguments), JsonNode.class);
    }

    /**
     * chat-completion toolMessage
     */
    public ToolMessage executeAndConvertToChatMessage(String funName, JsonNode arguments, String toolId) {
        return new ToolMessage(executeAndConvertToJson(funName, arguments).toPrettyString(), toolId);
    }

    public Future<ToolMessage> executeAndConvertToChatMessageAsync(String funName, JsonNode arguments, String toolId) {
        return executorService.submit(() -> executeAndConvertToChatMessage(funName, arguments, toolId));
    }

    /**
     * assistant stream toolMessage
     */
    public SubmitToolOutputRequestItem executeAndConvertToSubmitToolOutputRequestItem(String funName, JsonNode arguments, String toolId) {
        return new SubmitToolOutputRequestItem(executeAndConvertToJson(funName, arguments).toPrettyString(), toolId);
    }

    public Future<SubmitToolOutputRequestItem> executeAndConvertToSubmitToolOutputRequestItemAsync(String funName, JsonNode arguments, String toolId) {
        return executorService.submit(() -> executeAndConvertToSubmitToolOutputRequestItem(funName, arguments, toolId));
    }

    /**
     * @deprecated see {@link ToolMessage}{@link #executeAndConvertToChatMessage(String, JsonNode, String)}
     */
    @Deprecated
    public FunctionMessage executeAndConvertToChatMessage(String funName, JsonNode arguments) {
        return new FunctionMessage(executeAndConvertToJson(funName, arguments).toPrettyString(),funName);
    }

    @Deprecated
    public Future<FunctionMessage> executeAndConvertToChatMessageAsync(String funName, JsonNode arguments) {
        return executorService.submit(() -> executeAndConvertToChatMessage(funName, arguments));
    }
}
