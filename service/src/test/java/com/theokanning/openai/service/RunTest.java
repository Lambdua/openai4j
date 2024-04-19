package com.theokanning.openai.service;

import com.theokanning.openai.OpenAiResponse;
import com.theokanning.openai.assistants.assistant.Assistant;
import com.theokanning.openai.assistants.assistant.AssistantRequest;
import com.theokanning.openai.assistants.message.Message;
import com.theokanning.openai.assistants.message.MessageRequest;
import com.theokanning.openai.assistants.run.Run;
import com.theokanning.openai.assistants.run.RunCreateRequest;
import com.theokanning.openai.assistants.thread.Thread;
import com.theokanning.openai.assistants.thread.ThreadRequest;
import com.theokanning.openai.utils.TikTokensUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RunTest {
    String token = System.getenv("OPENAI_TOKEN");
    OpenAiService service = new OpenAiService(token);

    @Test
    @Timeout(10)
    void createRetrieveRun() {
        AssistantRequest assistantRequest = AssistantRequest.builder()
                .model(TikTokensUtil.ModelEnum.GPT_4_1106_preview.getName())
                .name("MATH_TUTOR")
                .instructions("You are a personal Math Tutor.")
                .build();
        Assistant assistant = service.createAssistant(assistantRequest);

        ThreadRequest threadRequest = ThreadRequest.builder()
                .build();
        Thread thread = service.createThread(threadRequest);

        MessageRequest messageRequest = MessageRequest.builder()
                .content("Hello")
                .build();

        Message message = service.createMessage(thread.getId(), messageRequest);

        RunCreateRequest runCreateRequest = RunCreateRequest.builder()
                .assistantId(assistant.getId())
                .build();

        Run run = service.createRun(thread.getId(), runCreateRequest);
        assertNotNull(run);

        Run retrievedRun;
        do {
            retrievedRun = service.retrieveRun(thread.getId(), run.getId());
            assertEquals(run.getId(), retrievedRun.getId());
        }
        while (!(retrievedRun.getStatus().equals("completed")) && !(retrievedRun.getStatus().equals("failed")));


        assertNotNull(retrievedRun);

        OpenAiResponse<Message> response = service.listMessages(thread.getId());

        List<Message> messages = response.getData();
        assertEquals(2, messages.size());
        assertEquals("user", messages.get(1).getRole());
        assertEquals("assistant", messages.get(0).getRole());
    }
}
