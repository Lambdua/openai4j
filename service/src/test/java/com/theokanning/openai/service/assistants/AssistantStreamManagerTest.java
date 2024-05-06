package com.theokanning.openai.service.assistants;

import com.theokanning.openai.assistants.message.Message;
import com.theokanning.openai.assistants.message.content.Delta;
import com.theokanning.openai.assistants.message.content.MessageDelta;
import com.theokanning.openai.assistants.run.Run;
import com.theokanning.openai.assistants.run_step.RunStep;
import com.theokanning.openai.assistants.run_step.RunStepDelta;
import com.theokanning.openai.service.assistant_stream.AssistantEventHandler;
import com.theokanning.openai.service.assistant_stream.AssistantResponseBodyCallback;
import com.theokanning.openai.service.assistant_stream.AssistantSSE;
import com.theokanning.openai.service.assistant_stream.AssistantStreamManager;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;
import retrofit2.Call;
import retrofit2.mock.Calls;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author LiangTao
 * @date 2024年05月02 11:21
 **/
@Slf4j
public class AssistantStreamManagerTest {
    public static Flowable<AssistantSSE> getAssistantStreamExample(String fileName) throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/" + fileName);
        String content = new BufferedReader(new InputStreamReader(fileInputStream)).lines().collect(Collectors.joining("\n"));
        ResponseBody body = ResponseBody.create(MediaType.get("application/json"), content);
        Call<ResponseBody> call = Calls.response(body);
        return Flowable.<AssistantSSE>create(emitter -> call.enqueue(new AssistantResponseBodyCallback(emitter)), BackpressureStrategy.BUFFER)
                .concatMap(item -> Flowable.just(item).delay(10, TimeUnit.MILLISECONDS));
    }

    @Test
    void generalResponseStreamTest() throws FileNotFoundException {
        AssistantEventHandler mockEventHandler = mock(AssistantEventHandler.class);
        Flowable<AssistantSSE> assistantStreamExample = getAssistantStreamExample("assistant-stream-response.txt");
        AssistantStreamManager manager = new AssistantStreamManager(assistantStreamExample, mockEventHandler);
        manager.start();

        manager.waitForCompletion();
        // Verify that the event handler received the correct method calls
        verify(mockEventHandler, times(39)).onEvent(isA(AssistantSSE.class));
        verify(mockEventHandler, times(1)).onRunCreated(isA(Run.class));
        verify(mockEventHandler, times(1)).onRunInProgress(isA(Run.class));
        verify(mockEventHandler, times(1)).onRunCompleted(isA(Run.class));
        verify(mockEventHandler, times(1)).onRunStepCreated(isA(RunStep.class));
        verify(mockEventHandler, times(1)).onRunStepCompleted(isA(RunStep.class));
        verify(mockEventHandler, times(1)).onMessageCreated(isA(Message.class));
        verify(mockEventHandler, times(1)).onMessageInProgress(isA(Message.class));
        verify(mockEventHandler, times(1)).onMessageCompleted(isA(Message.class));
        verify(mockEventHandler, atLeast(1)).onMessageDelta(isA(MessageDelta.class));
        verify(mockEventHandler, times(1)).onEnd();
        verify(mockEventHandler, never()).onError(any(Throwable.class));
        verify(mockEventHandler, never()).onRunStepDelta(any(RunStepDelta.class));

        Message complectionMessage = manager.getCurrentMessage().get();
        assertEquals("completed", complectionMessage.getStatus());
        MessageDelta messageDelta = manager.getAccumulatedMsg().get();
        assertEquals(messageDelta.getId(), complectionMessage.getId());
        Delta accumulatedDelata = messageDelta.getDelta();
        assertEquals(accumulatedDelata.getRole(), complectionMessage.getRole());
        assertEquals(accumulatedDelata.getContent().get(0).getType(), complectionMessage.getContent().get(0).getType());
        assertEquals(accumulatedDelata.getContent().get(0).getText().getValue(), complectionMessage.getContent().get(0).getText().getValue());
    }


}
