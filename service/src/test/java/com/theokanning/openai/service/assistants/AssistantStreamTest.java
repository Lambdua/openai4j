package com.theokanning.openai.service.assistants;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.DeleteResult;
import com.theokanning.openai.assistants.StreamEvent;
import com.theokanning.openai.assistants.assistant.Assistant;
import com.theokanning.openai.assistants.assistant.AssistantRequest;
import com.theokanning.openai.assistants.assistant.FunctionTool;
import com.theokanning.openai.assistants.message.MessageRequest;
import com.theokanning.openai.assistants.run.CreateThreadAndRunRequest;
import com.theokanning.openai.assistants.run.ToolChoice;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author LiangTao
 * @date 2024年04月30 09:14
 **/
public class AssistantStreamTest {
    static OpenAiService service = new OpenAiService();
    static String assistantId;

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
    }

    @Test
    void testGeneralStreamTest() throws JsonProcessingException {
        Flowable<AssistantSSE> threadAndRunStream = service.createThreadAndRunStream(
                CreateThreadAndRunRequest.builder()
                        .assistantId(assistantId)
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

        TestSubscriber<AssistantSSE> testSubscriber = new TestSubscriber<>();
        threadAndRunStream.blockingSubscribe(testSubscriber);
        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        assertFalse(testSubscriber.values().isEmpty());
        AssistantSSE done = testSubscriber.values().get(testSubscriber.values().size() - 1);
        assertEquals(StreamEvent.DONE, done.getEvent());
        Optional<AssistantSSE> runStepCompletion = testSubscriber.values().stream().filter(item -> item.getEvent().equals(StreamEvent.THREAD_RUN_STEP_COMPLETED)).findFirst();
        assertTrue(runStepCompletion.isPresent());
        ObjectMapper objectMapper = new ObjectMapper();
        RunStep runStep = objectMapper.readValue(runStepCompletion.get().getData(), RunStep.class);
        assertEquals(runStep.getStepDetails().getType(), "message_creation");
    }


}
