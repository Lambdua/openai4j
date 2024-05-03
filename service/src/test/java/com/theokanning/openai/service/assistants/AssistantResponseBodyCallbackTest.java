package com.theokanning.openai.service.assistants;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.mock.Calls;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author LiangTao
 * @date 2024年04月29 11:04
 **/
public class AssistantResponseBodyCallbackTest {
    static ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void setUp() {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * 将 JSON 字符串转换为 JsonNode，并递归移除所有 null 值字段。
     *
     * @param jsonStr 输入的 JSON 字符串
     * @return 清理后的 JsonNode
     * @throws IOException 如果解析 JSON 字符串时发生错误
     */
    private static JsonNode convertToJsonNodeAndRemoveNulls(String jsonStr) throws IOException {
        JsonNode rootNode = objectMapper.readTree(jsonStr);
        return removeNulls(rootNode);
    }

    @Test
    void testEmitDone() {
        ResponseBody body = ResponseBody.create(MediaType.get("application/json"), "event: done\ndata: [DONE]\n\n");
        Call<ResponseBody> call = Calls.response(body);

        Flowable<AssistantSSE> flowable = Flowable.create(emitter -> call.enqueue(new AssistantResponseBodyCallback(emitter)), BackpressureStrategy.BUFFER);

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
        Flowable<AssistantSSE> flowable = Flowable.create(emitter -> call.enqueue(new AssistantResponseBodyCallback(emitter)), BackpressureStrategy.BUFFER);
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

        Flowable<AssistantSSE> flowable = Flowable.create(emitter -> call.enqueue(new AssistantResponseBodyCallback(emitter)), BackpressureStrategy.BUFFER);

        TestSubscriber<AssistantSSE> testSubscriber = new TestSubscriber<>();
        flowable.subscribe(testSubscriber);

        testSubscriber.assertError(OpenAiHttpException.class);

        assertEquals("No thread found with id 'thread_BaRB3gk3HbzVTzHq2ryfGakQ'.", testSubscriber.errors().get(0).getMessage());
    }

    /**
     * 递归移除 JsonNode 中的所有 null 值字段。
     *
     * @param node 需要被清理的 JsonNode
     * @return 清理后的 JsonNode
     */
    private static JsonNode removeNulls(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> iterator = objectNode.fields();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = iterator.next();
                if (entry.getValue().isNull()) {
                    iterator.remove();  // 移除值为 null 的字段
                } else {
                    removeNulls(entry.getValue());  // 递归处理
                }
            }
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (int i = 0; i < arrayNode.size(); i++) {
                removeNulls(arrayNode.get(i));  // 递归处理数组元素
            }
        }
        return node;
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "assistant-stream-codeInterpreter.txt",
            "assistant-stream-error.txt",
            "assistant-stream-fileSearch.txt",
            "assistant-stream-response.txt",
            "assistant-stream-tool-require.txt",
            "assistant-stream-submit-tool.txt"
    })
    void testStreamToolResponse(String streamExampleFile) throws IOException {
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/" + streamExampleFile);
        String content = new BufferedReader(new InputStreamReader(fileInputStream)).lines().collect(Collectors.joining("\n"));
        ResponseBody body = ResponseBody.create(MediaType.get("application/json"), content);
        Call<ResponseBody> call = Calls.response(body);

        Flowable<AssistantSSE> flowable = Flowable.create(emitter -> call.enqueue(new AssistantResponseBodyCallback(emitter)), BackpressureStrategy.BUFFER);

        TestSubscriber<AssistantSSE> testSubscriber = new TestSubscriber<>();
        flowable.subscribe(testSubscriber);
        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        for (AssistantSSE sse : testSubscriber.values()) {
            if (sse.getEvent().equals(StreamEvent.DONE)) {
                assertEquals("[DONE]", sse.getData());
                continue;
            }
            Object o = objectMapper.readValue(sse.getData(), sse.getEvent().dataClass);
            assertEquals(sse.getEvent().dataClass, o.getClass());
            String actual = objectMapper.writeValueAsString(o);
            assertEquals(convertToJsonNodeAndRemoveNulls(sse.getData()), objectMapper.readTree(actual));
        }
    }

}
