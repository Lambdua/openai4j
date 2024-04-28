package com.theokanning.openai.service.assistants;

import com.theokanning.openai.OpenAiResponse;
import com.theokanning.openai.assistants.assistant.Assistant;
import com.theokanning.openai.assistants.assistant.AssistantRequest;
import com.theokanning.openai.assistants.assistant.FileSearchTool;
import com.theokanning.openai.assistants.message.Message;
import com.theokanning.openai.assistants.message.MessageContent;
import com.theokanning.openai.assistants.message.MessageRequest;
import com.theokanning.openai.assistants.run.Run;
import com.theokanning.openai.assistants.run.RunCreateRequest;
import com.theokanning.openai.assistants.run.ToolChoice;
import com.theokanning.openai.assistants.thread.Attachment;
import com.theokanning.openai.assistants.thread.Thread;
import com.theokanning.openai.assistants.thread.ThreadRequest;
import com.theokanning.openai.file.File;
import com.theokanning.openai.service.OpenAiService;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RunTest {
    OpenAiService service = new OpenAiService();

    @Test
    void createRetrieveRun() {
        AssistantRequest assistantRequest = AssistantRequest.builder()
                .model("gpt-3.5-turbo")
                .name("文字检索")
                .instructions("你是一个中国传统音乐教授,负责根据用户的需求解答问题")
                .tools(Arrays.asList(
                        new FileSearchTool()
                ))
                .temperature(0D)
                .build();
        Assistant assistant = service.createAssistant(assistantRequest);
        ThreadRequest threadRequest = ThreadRequest.builder()
                .build();
        Thread thread = service.createThread(threadRequest);
        File file = service.uploadFile("assistants", "src/test/resources/田山歌中艺术特征及其共生性特征探析.txt");
        try {
            MessageRequest messageRequest = MessageRequest.builder()
                    .content("请你检索我提供的文件然后回答问题: 田山歌体裁中的包容性具体体现在什么地方?")
                    .attachments(Arrays.asList(
                            new Attachment(file.getId(), Arrays.asList(new FileSearchTool()))
                    ))
                    .build();

            Message message = service.createMessage(thread.getId(), messageRequest);

            RunCreateRequest runCreateRequest = RunCreateRequest.builder()
                    .assistantId(assistant.getId())
                    .toolChoice(ToolChoice.AUTO)
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
            Message responseMsg = messages.get(1);
            assertEquals(message.getId(), responseMsg.getId());
            assertEquals("user", responseMsg.getRole());
            assertEquals("assistant", messages.get(0).getRole());
            MessageContent responseContent = responseMsg.getContent().get(0);
            assertEquals("text", responseContent.getType());
            assertNotNull(responseContent.getText().getAnnotations());
            assertFalse(responseContent.getText().getAnnotations().isEmpty());
        } finally {
            try {
                service.deleteAssistant(assistant.getId());
            } catch (Exception e) {
                // ignore
            }
            try {
                service.deleteThread(thread.getId());
            } catch (Exception e) {
                // ignore
            }
            try {
                service.deleteFile(file.getId());
            } catch (Exception e) {
                // ignore
            }
        }
    }
}
