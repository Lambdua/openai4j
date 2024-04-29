package com.theokanning.openai.service.assistants;

import com.theokanning.openai.OpenAiHttpException;
import com.theokanning.openai.assistants.StreamEvent;
import com.theokanning.openai.service.SSEFormatException;
import com.theokanning.openai.service.assistant_stream.AssistantResponseBodyCallback;
import com.theokanning.openai.service.assistant_stream.AssistantSSE;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.mock.Calls;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author LiangTao
 * @date 2024年04月29 11:04
 **/
public class AssistantResponseBodyCallbackTest {
    @Test
    void testStreamToolResponse() throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/assistant-submit-tool-stream.txt");
        String content = new BufferedReader(new InputStreamReader(fileInputStream)).lines().collect(Collectors.joining("\n"));
        ResponseBody body = ResponseBody.create(MediaType.get("application/json"), content);
        Call<ResponseBody> call = Calls.response(body);

        Flowable<AssistantSSE> flowable = Flowable.create(emitter -> call.enqueue(new AssistantResponseBodyCallback(emitter, true)), BackpressureStrategy.BUFFER);

        TestSubscriber<AssistantSSE> testSubscriber = new TestSubscriber<>();
        flowable.subscribe(testSubscriber);

        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        assertEquals(StreamEvent.THREAD_RUN_STEP_COMPLETED, testSubscriber.values().get(0).getEvent());
        assertEquals(StreamEvent.THREAD_RUN_COMPLETED, testSubscriber.values().get(26).getEvent());
        testSubscriber.assertValueCount(28);
    }

    @Test
    void testStreamGeneralResponse() throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/assistant-stream-response.txt");
        String content = new BufferedReader(new InputStreamReader(fileInputStream)).lines().collect(Collectors.joining("\n"));
        ResponseBody body = ResponseBody.create(MediaType.get("application/json"), content);
        Call<ResponseBody> call = Calls.response(body);

        Flowable<AssistantSSE> flowable = Flowable.create(emitter -> call.enqueue(new AssistantResponseBodyCallback(emitter, true)), BackpressureStrategy.BUFFER);

        TestSubscriber<AssistantSSE> testSubscriber = new TestSubscriber<>();
        flowable.subscribe(testSubscriber);

        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        assertEquals(StreamEvent.THREAD_RUN_CREATED, testSubscriber.values().get(0).getEvent());
        assertEquals(StreamEvent.THREAD_RUN_COMPLETED, testSubscriber.values().get(37).getEvent());
        testSubscriber.assertValueCount(39);
    }


    @Test
    void testStreamToolRequire() throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/assistant-stream-tool-require.txt");
        String content = new BufferedReader(new InputStreamReader(fileInputStream)).lines().collect(Collectors.joining("\n"));
        ResponseBody body = ResponseBody.create(MediaType.get("application/json"), content);
        Call<ResponseBody> call = Calls.response(body);

        Flowable<AssistantSSE> flowable = Flowable.create(emitter -> call.enqueue(new AssistantResponseBodyCallback(emitter, true)), BackpressureStrategy.BUFFER);

        TestSubscriber<AssistantSSE> testSubscriber = new TestSubscriber<>();
        flowable.subscribe(testSubscriber);

        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        assertEquals(StreamEvent.THREAD_RUN_REQUIRES_ACTION, testSubscriber.values().get(20).getEvent());
        assertEquals(StreamEvent.THREAD_RUN_CREATED, testSubscriber.values().get(0).getEvent());
        testSubscriber.assertValueCount(22);
    }

    @Test
    void testEmitDone() {
        ResponseBody body = ResponseBody.create(MediaType.get("application/json"), "event: done\ndata: [DONE]\n\n");
        Call<ResponseBody> call = Calls.response(body);

        Flowable<AssistantSSE> flowable = Flowable.create(emitter -> call.enqueue(new AssistantResponseBodyCallback(emitter, true)), BackpressureStrategy.BUFFER);

        TestSubscriber<AssistantSSE> testSubscriber = new TestSubscriber<>();
        flowable.subscribe(testSubscriber);

        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(1);
        assertEquals("[DONE]", testSubscriber.values().get(0).getData());
        assertEquals(StreamEvent.DONE, testSubscriber.values().get(0).getEvent());
    }

    @Test
    void testSseFormatException() {
        ResponseBody body = ResponseBody.create(MediaType.get("application/json"), "event: done\ndata: line 2\ndata: [DONE]\n\n");
        Call<ResponseBody> call = Calls.response(body);
        Flowable<AssistantSSE> flowable = Flowable.create(emitter -> call.enqueue(new AssistantResponseBodyCallback(emitter, true)), BackpressureStrategy.BUFFER);
        TestSubscriber<AssistantSSE> testSubscriber = new TestSubscriber<>();
        flowable.subscribe(testSubscriber);
        testSubscriber.assertError(SSEFormatException.class);
    }

    @Test
    void testServerError() {
        String errorBody = "{\n" +
                "    \"error\": {\n" +
                "        \"message\": \"No thread found with id 'thread_BaRB3gk3HbzVTzHq2ryfGakQ'.\",\n" +
                "        \"type\": \"invalid_request_error\",\n" +
                "        \"param\": null,\n" +
                "        \"code\": null\n" +
                "    }\n" +
                "}";
        ResponseBody body = ResponseBody.create(MediaType.get("application/json"), errorBody);
        Call<ResponseBody> call = Calls.response(Response.error(401, body));

        Flowable<AssistantSSE> flowable = Flowable.create(emitter -> call.enqueue(new AssistantResponseBodyCallback(emitter, true)), BackpressureStrategy.BUFFER);

        TestSubscriber<AssistantSSE> testSubscriber = new TestSubscriber<>();
        flowable.subscribe(testSubscriber);

        testSubscriber.assertError(OpenAiHttpException.class);

        assertEquals("No thread found with id 'thread_BaRB3gk3HbzVTzHq2ryfGakQ'.", testSubscriber.errors().get(0).getMessage());
    }
}
