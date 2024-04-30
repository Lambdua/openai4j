package com.theokanning.openai.service.assistants;

import com.theokanning.openai.ListSearchParameters;
import com.theokanning.openai.OpenAiResponse;
import com.theokanning.openai.assistants.assistant.Assistant;
import com.theokanning.openai.assistants.assistant.AssistantRequest;
import com.theokanning.openai.assistants.assistant.FunctionTool;
import com.theokanning.openai.assistants.message.Message;
import com.theokanning.openai.assistants.message.MessageRequest;
import com.theokanning.openai.assistants.run.*;
import com.theokanning.openai.assistants.thread.Thread;
import com.theokanning.openai.assistants.thread.ThreadRequest;
import com.theokanning.openai.service.FunctionExecutor;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.service.util.ToolUtil;
import org.junit.jupiter.api.*;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 测试assistant tool require action的能力
 *
 * @author LiangTao
 * @date 2024年04月28 13:37
 **/
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AssistantToolTest {
    static OpenAiService service = new OpenAiService();


    static String assistantId;

    static String threadId;

    static FunctionExecutor executor;

    @BeforeAll
    static void initial() {
        executor = new FunctionExecutor(Collections.singletonList(ToolUtil.weatherFunction()));
        AssistantRequest assistantRequest = AssistantRequest.builder()
                .model("gpt-3.5-turbo").name("weather assistant")
                .instructions("You are a weather assistant responsible for calling the weather API to return weather information based on the location entered by the user")
                .tools(Collections.singletonList(new FunctionTool(ToolUtil.weatherFunction())))
                .temperature(0D)
                .build();
        Assistant assistant = service.createAssistant(assistantRequest);
        assistantId = assistant.getId();
        ThreadRequest threadRequest = ThreadRequest.builder()
                .build();
        Thread thread = service.createThread(threadRequest);
        threadId = thread.getId();
    }

    @AfterAll
    static void deleteTestData() {
        try {
            service.deleteAssistant(assistantId);
        } catch (Exception e) {
            // ignore
        }
        try {
            service.deleteThread(threadId);
        } catch (Exception e) {
            // ignore
        }
    }

    @Test
    void weatherFunctionCallTest() {
        MessageRequest messageRequest = MessageRequest.builder()
                .content("What's the weather of Xiamen?")
                .build();
        //add message to thread
        service.createMessage(threadId, messageRequest);
        RunCreateRequest runCreateRequest = RunCreateRequest.builder().assistantId(assistantId).build();

        Run run = service.createRun(threadId, runCreateRequest);
        assertNotNull(run);

        Run retrievedRun = service.retrieveRun(threadId, run.getId());
        while (!(retrievedRun.getStatus().equals("completed"))
                && !(retrievedRun.getStatus().equals("failed"))
                && !(retrievedRun.getStatus().equals("expired"))
                && !(retrievedRun.getStatus().equals("incomplete"))
                && !(retrievedRun.getStatus().equals("requires_action"))) {
            retrievedRun = service.retrieveRun(threadId, run.getId());
        }
        assertEquals("requires_action", retrievedRun.getStatus());
        RequiredAction requiredAction = retrievedRun.getRequiredAction();
        List<ToolCall> toolCalls = requiredAction.getSubmitToolOutputs().getToolCalls();
        ToolCall toolCall = toolCalls.get(0);
        ToolCallFunction function = toolCall.getFunction();
        String toolCallId = toolCall.getId();

        SubmitToolOutputRequestItem toolOutputRequestItem = SubmitToolOutputRequestItem.builder()
                .toolCallId(toolCallId)
                .output(executor.executeAndConvertToJson(function).toPrettyString())
                .build();
        List<SubmitToolOutputRequestItem> toolOutputRequestItems = Collections.singletonList(toolOutputRequestItem);
        SubmitToolOutputsRequest submitToolOutputsRequest = SubmitToolOutputsRequest.builder()
                .toolOutputs(toolOutputRequestItems)
                .build();
        retrievedRun = service.submitToolOutputs(threadId, retrievedRun.getId(), submitToolOutputsRequest);

        while (!(retrievedRun.getStatus().equals("completed"))
                && !(retrievedRun.getStatus().equals("failed"))
                && !(retrievedRun.getStatus().equals("expired"))
                && !(retrievedRun.getStatus().equals("incomplete"))
                && !(retrievedRun.getStatus().equals("requires_action"))) {
            retrievedRun = service.retrieveRun(threadId, run.getId());
        }
        assertEquals("completed", retrievedRun.getStatus());
        OpenAiResponse<Message> response = service.listMessages(threadId, new ListSearchParameters());
        List<Message> messages = response.getData();
        assertNotNull(messages);
        assertEquals(2, messages.size());
    }

}
