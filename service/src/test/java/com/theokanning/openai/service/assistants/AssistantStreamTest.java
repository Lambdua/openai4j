package com.theokanning.openai.service.assistants;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.DeleteResult;
import com.theokanning.openai.assistants.StreamEvent;
import com.theokanning.openai.assistants.assistant.Assistant;
import com.theokanning.openai.assistants.assistant.AssistantRequest;
import com.theokanning.openai.assistants.assistant.FunctionTool;
import com.theokanning.openai.assistants.message.Message;
import com.theokanning.openai.assistants.message.MessageRequest;
import com.theokanning.openai.assistants.run.*;
import com.theokanning.openai.assistants.run_step.RunStep;
import com.theokanning.openai.assistants.thread.ThreadRequest;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.service.assistant_stream.AssistantSSE;
import com.theokanning.openai.service.util.ToolUtil;
import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author LiangTao
 * @date 2024年04月30 09:14
 **/
public class AssistantStreamTest {
    static OpenAiService service = new OpenAiService();
    static String assistantId;
    static String threadId;

    @BeforeAll
    static void initial() {
        AssistantRequest assistantRequest = AssistantRequest.builder()
                .model("gpt-3.5-turbo").name("weather assistant")
                .instructions("You are a weather assistant responsible for calling the weather API to return weather information based on the location entered by the user")
                .tools(Collections.singletonList(new FunctionTool(ToolUtil.weatherFunction())))
                .temperature(0D)
                .build();
        Assistant assistant = service.createAssistant(assistantRequest);
        assistantId = assistant.getId();
    }

    @AfterAll
    static void deleteTestData() {
        try {
            DeleteResult deleteResult = service.deleteAssistant(assistantId);
            assertTrue(deleteResult.isDeleted());
        } catch (Exception e) {
            // ignore
        }
        try {
            DeleteResult deleteResult = service.deleteThread(threadId);
            assertTrue(deleteResult.isDeleted());
        } catch (Exception e) {
            // ignore
        }
    }

    @Test
    void streamGeneralResponseAndFunctionCallTest() throws JsonProcessingException {
        //测试1: 测试一般响应
        Flowable<AssistantSSE> threadAndRunStream = service.createThreadAndRunStream(
                CreateThreadAndRunRequest.builder()
                        .assistantId(assistantId)
                        //这里不使用任何工具
                        .toolChoice(ToolChoice.NONE)
                        .thread(ThreadRequest.builder()
                                .messages(Collections.singletonList(
                                        MessageRequest.builder()
                                                .content("你好,你可以帮助我做什么?")
                                                .build()
                                ))
                                .build())
                        .build()
        );

        TestSubscriber<AssistantSSE> subscriber1 = new TestSubscriber<>();
        threadAndRunStream.blockingSubscribe(subscriber1);
        subscriber1.assertComplete();
        subscriber1.assertNoErrors();
        assertFalse(subscriber1.values().isEmpty());
        AssistantSSE done = subscriber1.values().get(subscriber1.values().size() - 1);
        assertEquals(StreamEvent.DONE, done.getEvent());
        Optional<AssistantSSE> runStepCompletion = subscriber1.values().stream().filter(item -> item.getEvent().equals(StreamEvent.THREAD_RUN_STEP_COMPLETED)).findFirst();
        assertTrue(runStepCompletion.isPresent());
        ObjectMapper objectMapper = new ObjectMapper();
        RunStep runStep = objectMapper.readValue(runStepCompletion.get().getData(), RunStep.class);
        assertEquals(runStep.getStepDetails().getType(), "message_creation");


        //测试2: 测试函数调用
        threadId = runStep.getThreadId();
        service.createMessage(threadId, MessageRequest.builder().content("请帮我查询北京天气").build());
        Flowable<AssistantSSE> getWeatherFlowable = service.createRunStream(threadId, RunCreateRequest.builder()
                //这里强制使用get_weather函数
                .assistantId(assistantId)
                .toolChoice(new ToolChoice(new Function("get_weather")))
                .build()
        );

        TestSubscriber<AssistantSSE> subscriber2 = new TestSubscriber<>();
        getWeatherFlowable.blockingSubscribe(subscriber2);
        subscriber2.assertComplete();
        subscriber2.assertNoErrors();
        assertFalse(subscriber2.values().isEmpty());
        AssistantSSE requireActionSse = subscriber2.values().get(subscriber2.values().size() - 2);
        assertEquals(StreamEvent.THREAD_RUN_REQUIRES_ACTION, requireActionSse.getEvent());
        Run requireActionRun = objectMapper.readValue(requireActionSse.getData(), Run.class);
        RequiredAction requiredAction = requireActionRun.getRequiredAction();
        assertNotNull(requiredAction);
        assertEquals("submit_tool_outputs", requiredAction.getType());
        List<ToolCall> toolCalls = requiredAction.getSubmitToolOutputs().getToolCalls();
        assertEquals(1, toolCalls.size());
        ToolCall toolCall = toolCalls.get(0);
        String callId = toolCall.getId();
        String location = toolCall.getFunction().getArguments().get("location").asText();
        assertNotNull(location);

        //测试3: 测试函数调用的响应
        Flowable<AssistantSSE> toolCallResponseFlowable = service.submitToolOutputsStream(threadId, requireActionRun.getId(), SubmitToolOutputsRequest.ofSingletonToolOutput(callId, "北京的天气是晴天"));
        TestSubscriber<AssistantSSE> subscriber3 = new TestSubscriber<>();
        toolCallResponseFlowable.blockingSubscribe(subscriber3);
        subscriber3.assertComplete();
        subscriber3.assertNoErrors();
        assertFalse(subscriber3.values().isEmpty());
        AssistantSSE toolCallResponse = subscriber3.values().get(subscriber3.values().size() - 2);
        assertEquals(StreamEvent.THREAD_RUN_COMPLETED, toolCallResponse.getEvent());
        Run run = objectMapper.readValue(toolCallResponse.getData(), Run.class);
        assertNotNull(run);
        Optional<AssistantSSE> msgSse = subscriber3.values().stream().filter(item -> StreamEvent.THREAD_MESSAGE_COMPLETED.equals(item.getEvent())).findFirst();
        assertTrue(msgSse.isPresent());
        Message message = objectMapper.readValue(msgSse.get().getData(), Message.class);
        String responseContent = message.getContent().get(0).getText().getValue();
        assertNotNull(responseContent);
    }


}
