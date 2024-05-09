package com.theokanning.openai.service;

import com.theokanning.openai.DeleteResult;
import com.theokanning.openai.assistants.assistant.CodeInterpreterTool;
import com.theokanning.openai.assistants.message.Message;
import com.theokanning.openai.assistants.message.MessageListSearchParameters;
import com.theokanning.openai.assistants.message.MessageRequest;
import com.theokanning.openai.assistants.message.ModifyMessageRequest;
import com.theokanning.openai.assistants.thread.Attachment;
import com.theokanning.openai.assistants.thread.ThreadRequest;
import com.theokanning.openai.file.File;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class MessageTest {

    static OpenAiService service;

    static String threadId;
    static String fileId;

    static String messageId;

    @BeforeAll
    static void setup() {
        service = new OpenAiService();

        ThreadRequest threadRequest = ThreadRequest.builder()
                .build();
        threadId = service.createThread(threadRequest).getId();
        File file = service.uploadFile("assistants", "src/test/resources/penguin.png");
        fileId = file.getId();
    }

    @AfterAll
    static void teardown() {
        try {
            service.deleteThread(threadId);
        } catch (Exception e) {
            // ignore
        }
        try {
            service.deleteFile(fileId);
        } catch (Exception e) {
            // ignore
        }
    }

    @Test
    void createMessage() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("key", "value");

        MessageRequest messageRequest = MessageRequest.builder()
                .content("Hello")
                .metadata(metadata)
                .attachments(Arrays.asList(new Attachment(fileId, Arrays.asList(new CodeInterpreterTool()))))
                .build();

        Message message = service.createMessage(threadId, messageRequest);
        messageId = message.getId();

        assertNotNull(message.getId());
        assertEquals("thread.message", message.getObject());
    }

    @Test
    void retrieveMessage() {
        Message message = service.retrieveMessage(threadId, messageId);
        assertEquals(messageId, message.getId());
        assertEquals("Hello", message.getContent().get(0).getText().getValue());
        assertEquals("value", message.getMetadata().get("key"));
        assertEquals("code_interpreter", message.getAttachments().get(0).getTools().get(0).getType());
        assertEquals(fileId, message.getAttachments().get(0).getFileId());
    }

    @Test
    void modifyMessage() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("key", "modify");
        ModifyMessageRequest request = ModifyMessageRequest.builder()
                .metadata(metadata)
                .build();
        Message message = service.modifyMessage(threadId, messageId, request);
        assertEquals(messageId, message.getId());
        assertEquals("modify", message.getMetadata().get("key"));
    }

    @Test
    void listMessages() {
        ThreadRequest threadRequest = ThreadRequest.builder().build();
        String separateThreadId = service.createThread(threadRequest).getId();
        createTestMessage(separateThreadId);
        createTestMessage(separateThreadId);
        createTestMessage(separateThreadId);
        List<Message> messages = service.listMessages(separateThreadId, new MessageListSearchParameters()).getData();
        assertEquals(3, messages.size());
        DeleteResult deleteResult = service.deleteThread(separateThreadId);
        assertTrue(deleteResult.isDeleted());
    }

    Message createTestMessage(String threadId) {
        MessageRequest messageRequest = MessageRequest.builder()
                .content("Hello")
                .build();

        return service.createMessage(threadId, messageRequest);
    }
}
