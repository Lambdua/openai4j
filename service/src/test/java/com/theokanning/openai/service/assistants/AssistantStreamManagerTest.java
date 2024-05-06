package com.theokanning.openai.service.assistants;

import com.theokanning.openai.OpenAiHttpException;
import com.theokanning.openai.assistants.StreamEvent;
import com.theokanning.openai.assistants.message.Message;
import com.theokanning.openai.assistants.message.content.Annotation;
import com.theokanning.openai.assistants.message.content.Delta;
import com.theokanning.openai.assistants.message.content.MessageDelta;
import com.theokanning.openai.assistants.run.Run;
import com.theokanning.openai.assistants.run.ToolCall;
import com.theokanning.openai.assistants.run_step.RunStep;
import com.theokanning.openai.assistants.run_step.RunStepDelta;
import com.theokanning.openai.service.assistant_stream.*;
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
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
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
        AssistantStreamManager manager = AssistantStreamManager.start(assistantStreamExample, mockEventHandler);


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
        assertEquals(StreamEvent.DONE, manager.getCurrentEvent().get());

        List<MessageDelta> msgDeltas = manager.getMsgDeltas();
        MessageDelta first = msgDeltas.get(0);
        for (int i = 1; i < msgDeltas.size(); i++) {
            first = DeltaUtil.accumulatMessageDelta(first, msgDeltas.get(i));
        }
        assertEquals(first.getDelta().getContent().get(0).getText().getValue(), accumulatedDelata.getContent().get(0).getText().getValue(), "MessageDelta should be accumulated correctly");

    }

    @Test
    void errorStreamTest() throws FileNotFoundException {
        AssistantEventHandler mockEventHandler = mock(AssistantEventHandler.class);
        Flowable<AssistantSSE> assistantStreamExample = getAssistantStreamExample("assistant-stream-error.txt");
        AssistantStreamManager manager = AssistantStreamManager.syncStart(assistantStreamExample, mockEventHandler);

        // Verify that the event handler received the correct method calls
        verify(mockEventHandler, times(1)).onError(isA(OpenAiHttpException.class));
        verify(mockEventHandler, times(1)).onEnd();

        AssistantSSE errorSse = manager.getEventMsgsHolder().stream().filter(sse -> StreamEvent.ERROR.equals(sse.getEvent())).findFirst().orElse(null);
        assertNotNull(errorSse, "Error event not found");
    }

    @Test
    void shutdownTest() throws FileNotFoundException, InterruptedException {
        AssistantEventHandler mockEventHandler = mock(AssistantEventHandler.class);
        Flowable<AssistantSSE> assistantStreamExample = getAssistantStreamExample("assistant-stream-response.txt");
        AssistantStreamManager manager = AssistantStreamManager.start(assistantStreamExample, mockEventHandler);

        Thread.sleep(200);
        manager.shutDown();
        assertFalse(manager.isCompleted());

        // Thread.sleep(1000);
        List<AssistantSSE> eventMsgsHolder = manager.getEventMsgsHolder();
        assertFalse(eventMsgsHolder.isEmpty());
        verify(mockEventHandler, never()).onEnd();
        verify(mockEventHandler, times(1)).onRunCreated(any(Run.class));
        Thread.sleep(1000);
        assertEquals(eventMsgsHolder.size(), manager.getEventMsgsHolder().size(), "eventMsgsHolder should not be updated after shutdown");
    }


    @Test
    void fileSearchStreamTest() throws FileNotFoundException {
        AssistantEventHandler mockEventHandler = mock(AssistantEventHandler.class);
        Flowable<AssistantSSE> assistantStreamExample = getAssistantStreamExample("assistant-stream-fileSearch.txt");
        AssistantStreamManager manager = AssistantStreamManager.syncStart(assistantStreamExample, mockEventHandler);

        // Verify that the event handler received the correct method calls
        verify(mockEventHandler, times(1)).onRunCreated(isA(Run.class));
        verify(mockEventHandler, times(1)).onRunInProgress(isA(Run.class));
        verify(mockEventHandler, times(1)).onRunCompleted(isA(Run.class));
        verify(mockEventHandler, times(2)).onRunStepCreated(isA(RunStep.class));
        verify(mockEventHandler, times(2)).onRunStepInProgress(isA(RunStep.class));
        verify(mockEventHandler, times(2)).onRunStepCompleted(isA(RunStep.class));
        verify(mockEventHandler, atLeastOnce()).onRunStepDelta(isA(RunStepDelta.class));
        verify(mockEventHandler, times(1)).onMessageCreated(isA(Message.class));
        verify(mockEventHandler, times(1)).onMessageInProgress(isA(Message.class));
        verify(mockEventHandler, times(1)).onMessageCompleted(isA(Message.class));
        verify(mockEventHandler, atLeastOnce()).onMessageDelta(isA(MessageDelta.class));
        verify(mockEventHandler, times(1)).onEnd();

        //runStepDelta should be accumulated correctly
        List<RunStepDelta> runStepDeltas = manager.getRunStepDeltas();
        assertFalse(runStepDeltas.isEmpty());
        RunStepDelta accumulatedRsd = manager.getAccumulatedRsd().orElse(null);
        assertNotNull(accumulatedRsd);
        assertEquals("tool_calls", accumulatedRsd.getDelta().getStepDetails().getType());
        assertEquals("file_search", accumulatedRsd.getDelta().getStepDetails().getToolCalls().get(0).getType());

        Message complectionMessage = manager.getCurrentMessage().get();
        assertEquals("completed", complectionMessage.getStatus());
        MessageDelta messageDelta = manager.getAccumulatedMsg().get();
        assertEquals(messageDelta.getId(), complectionMessage.getId());
        Delta accumulatedDelata = messageDelta.getDelta();
        assertEquals(accumulatedDelata.getRole(), complectionMessage.getRole());
        assertEquals(accumulatedDelata.getContent().get(0).getType(), complectionMessage.getContent().get(0).getType());
        assertEquals(accumulatedDelata.getContent().get(0).getText().getValue(), complectionMessage.getContent().get(0).getText().getValue());
        List<Annotation> accumulateAnnotations = accumulatedDelata.getContent().get(0).getText().getAnnotations();
        assertNotNull(accumulateAnnotations, "MessageDelta should be accumulated correctly");
        List<Annotation> responseAnnotations = complectionMessage.getContent().get(0).getText().getAnnotations();
        assertNotNull(responseAnnotations, "MessageDelta should be accumulated correctly");
        assertEquals(accumulateAnnotations.size(), responseAnnotations.size(), "MessageDelta should be accumulated correctly");
        for (Annotation accumulateAnnotation : accumulateAnnotations) {
            Annotation raItem = responseAnnotations.stream().filter(ra -> ra.getText().equals(accumulateAnnotation.getText())).findFirst().orElse(null);
            assertNotNull(raItem, "MessageDelta should be accumulated correctly");
            assertEquals(raItem.getFileCitation().getFileId(), accumulateAnnotation.getFileCitation().getFileId(), "MessageDelta should be accumulated correctly");
            assertEquals(raItem.getStartIndex(), accumulateAnnotation.getStartIndex(), "MessageDelta should be accumulated correctly");
            assertEquals(raItem.getEndIndex(), accumulateAnnotation.getEndIndex(), "MessageDelta should be accumulated correctly");
        }

        List<MessageDelta> msgDeltas = manager.getMsgDeltas();
        MessageDelta first = msgDeltas.get(0);
        for (int i = 1; i < msgDeltas.size(); i++) {
            first = DeltaUtil.accumulatMessageDelta(first, msgDeltas.get(i));
        }
        assertEquals(first.getDelta().getContent().get(0).getText().getValue(), accumulatedDelata.getContent().get(0).getText().getValue(), "MessageDelta should be accumulated correctly");
    }

    @Test
    void toolRequireStreamTest() throws FileNotFoundException {
        AssistantEventHandler mockEventHandler = mock(AssistantEventHandler.class);
        Flowable<AssistantSSE> assistantStreamExample = getAssistantStreamExample("assistant-stream-tool-require.txt");
        AssistantStreamManager manager = AssistantStreamManager.syncStart(assistantStreamExample, mockEventHandler);

        // Verify that the event handler received the correct method calls
        verify(mockEventHandler, times(1)).onRunCreated(isA(Run.class));
        verify(mockEventHandler, times(1)).onRunInProgress(isA(Run.class));
        verify(mockEventHandler, times(1)).onRunStepCreated(isA(RunStep.class));
        verify(mockEventHandler, times(1)).onRunStepInProgress(isA(RunStep.class));
        verify(mockEventHandler, atLeastOnce()).onRunStepDelta(isA(RunStepDelta.class));
        verify(mockEventHandler, never()).onMessageCreated(isA(Message.class));
        verify(mockEventHandler, never()).onMessageInProgress(isA(Message.class));
        verify(mockEventHandler, never()).onMessageCompleted(isA(Message.class));
        verify(mockEventHandler, never()).onMessageDelta(isA(MessageDelta.class));
        verify(mockEventHandler, times(1)).onRunRequiresAction(isA(Run.class));
        verify(mockEventHandler, times(1)).onEnd();

        //runStepDelta should be accumulated correctly
        List<RunStepDelta> runStepDeltas = manager.getRunStepDeltas();
        assertFalse(runStepDeltas.isEmpty());
        RunStepDelta accumulatedRsd = manager.getAccumulatedRsd().orElse(null);
        assertNotNull(accumulatedRsd);
        assertEquals("tool_calls", accumulatedRsd.getDelta().getStepDetails().getType());
        ToolCall accumulateTool = accumulatedRsd.getDelta().getStepDetails().getToolCalls().get(0);
        assertEquals("function", accumulateTool.getType());

        Run lastRun = manager.getCurrentRun().orElse(null);
        assertNotNull(lastRun);
        assertEquals("requires_action", lastRun.getStatus());
        List<ToolCall> responseToolCalls = lastRun.getRequiredAction().getSubmitToolOutputs().getToolCalls();
        assertNotNull(responseToolCalls);
        assertEquals(1, responseToolCalls.size());

        ToolCall respTool = responseToolCalls.get(0);

        //for use equals method,index should be null
        accumulateTool.setIndex(null);
        assertEquals(respTool, accumulateTool);
    }

    @Test
    void codeInterpreterStreamTest() throws FileNotFoundException {
        AssistantEventHandler mockEventHandler = mock(AssistantEventHandler.class);
        Flowable<AssistantSSE> assistantStreamExample = getAssistantStreamExample("assistant-stream-code-interpreter.txt");
        AssistantStreamManager manager = AssistantStreamManager.syncStart(assistantStreamExample, mockEventHandler);

        // Verify that the event handler received the correct method calls
        verify(mockEventHandler, atLeastOnce()).onRunStepDelta(isA(RunStepDelta.class));
        verify(mockEventHandler, times(1)).onEnd();

        //runStepDelta should be accumulated correctly
        List<RunStepDelta> runStepDeltas = manager.getRunStepDeltas();
        assertFalse(runStepDeltas.isEmpty());
        RunStepDelta accumulatedRsd = manager.getAccumulatedRsd().orElse(null);
        assertNotNull(accumulatedRsd);
        assertEquals("tool_calls", accumulatedRsd.getDelta().getStepDetails().getType());
        ToolCall accumulateTool = accumulatedRsd.getDelta().getStepDetails().getToolCalls().get(0);
        assertEquals("code_interpreter", accumulateTool.getType());

        AssistantSSE respRsd = manager.getEventMsgsHolder().stream().filter(sse -> sse.getEvent().equals(StreamEvent.THREAD_RUN_STEP_COMPLETED) && ((RunStep) sse.getPojo()).getId().equals(accumulatedRsd.getId())).findFirst().orElse(null);
        assertNotNull(respRsd);
        RunStep codeInterpreterRunStep = respRsd.getPojo();
        ToolCall respTool = codeInterpreterRunStep.getStepDetails().getToolCalls().get(0);

        assertEquals("code_interpreter", respTool.getType());


        //for use equals method,index should be null
        accumulateTool.setIndex(null);
        accumulateTool.getCodeInterpreter().getOutputs().get(0).setIndex(null);
        assertEquals(respTool, accumulateTool);


    }

}
