package com.theokanning.openai.service.assistants;

import com.theokanning.openai.DeleteResult;
import com.theokanning.openai.assistants.message.MessageRequest;
import com.theokanning.openai.assistants.thread.Thread;
import com.theokanning.openai.assistants.thread.ThreadRequest;
import com.theokanning.openai.service.OpenAiService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ThreadTest {

    OpenAiService service = new OpenAiService();

    static String threadId;

    @Test
    @Order(1)
    void createThread() {
        MessageRequest messageRequest = MessageRequest.builder()
                .content("Hello")
                .role("user")
                .build();

        ThreadRequest threadRequest = ThreadRequest.builder()
                .messages(Collections.singletonList(messageRequest))
                .metadata(Collections.singletonMap("action", "create"))
                .build();

        Thread thread = service.createThread(threadRequest);
        threadId = thread.getId();
        assertEquals("thread", thread.getObject());
    }

    @Test
    @Order(2)
    void retrieveThread() {
        Thread thread = service.retrieveThread(threadId);
        assertEquals("create", thread.getMetadata().get("action"));
        assertEquals("thread", thread.getObject());
    }

    @Test
    @Order(3)
    void modifyThread() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("action", "modify");
        ThreadRequest threadRequest = ThreadRequest.builder()
                .metadata(metadata)
                .build();
        Thread thread = service.modifyThread(threadId, threadRequest);
        assertEquals("thread", thread.getObject());
        assertEquals("modify", thread.getMetadata().get("action"));
    }

    @Test
    @Order(4)
    void deleteThread() {
        DeleteResult deleteResult = service.deleteThread(threadId);
        assertEquals("thread.deleted", deleteResult.getObject());
    }
}
