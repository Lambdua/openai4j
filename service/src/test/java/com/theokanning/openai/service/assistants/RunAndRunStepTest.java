package com.theokanning.openai.service.assistants;

import com.theokanning.openai.ListSearchParameters;
import com.theokanning.openai.OpenAiResponse;
import com.theokanning.openai.assistants.assistant.Assistant;
import com.theokanning.openai.assistants.assistant.AssistantRequest;
import com.theokanning.openai.assistants.assistant.FileSearchTool;
import com.theokanning.openai.assistants.message.MessageRequest;
import com.theokanning.openai.assistants.run.*;
import com.theokanning.openai.assistants.run_step.RunStep;
import com.theokanning.openai.assistants.thread.Attachment;
import com.theokanning.openai.assistants.thread.Thread;
import com.theokanning.openai.assistants.thread.ThreadRequest;
import com.theokanning.openai.file.File;
import com.theokanning.openai.service.OpenAiService;
import org.junit.jupiter.api.*;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RunAndRunStepTest {
    static OpenAiService service = new OpenAiService();

    static String assistantId;

    static String threadId;

    static String fileId;

    static String runId;

    static String runThreadId;

    static String runThreadRunId;


    @BeforeAll
    static void initial() {
        AssistantRequest assistantRequest = AssistantRequest.builder()
                .model("gpt-3.5-turbo")
                .name("文字检索")
                .instructions("你是一个中国传统音乐教授,负责根据用户的需求解答问题")
                .tools(Collections.singletonList(new FileSearchTool()))
                .temperature(0D)
                .build();
        Assistant assistant = service.createAssistant(assistantRequest);
        assistantId = assistant.getId();
        ThreadRequest threadRequest = ThreadRequest.builder()
                .build();
        Thread thread = service.createThread(threadRequest);
        threadId = thread.getId();
        File file = service.uploadFile("assistants", "src/test/resources/田山歌中艺术特征及其共生性特征探析.txt");
        fileId = file.getId();
    }

    @AfterAll
    static void deleteTestData() {
        try {
            service.deleteAssistant(assistantId);
        } catch (Exception e) {
            // ignore
        }
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
        try {
            service.deleteThread(runThreadId);
        } catch (Exception e) {
            // ignore
        }
    }

    @Test
    @Order(1)
    void createRun() {
        MessageRequest messageRequest = MessageRequest.builder()
                .content("请你检索我提供的文件然后回答问题: 田山歌体裁中的包容性具体体现在什么地方?")
                .attachments(Collections.singletonList(
                        new Attachment(fileId, Collections.singletonList(new FileSearchTool()))
                ))
                .build();
        //add msg to thread
        service.createMessage(threadId, messageRequest);

        RunCreateRequest runCreateRequest = RunCreateRequest.builder()
                .assistantId(assistantId)
                .toolChoice(ToolChoice.AUTO)
                .metadata(Collections.singletonMap("action", "create"))
                .build();
        Run run = service.createRun(threadId, runCreateRequest);
        runId = run.getId();
        assertNotNull(run);
    }


    @Test
    @Order(2)
    void cancelRun() {
        Run run = service.cancelRun(threadId, runId);
        assertEquals("cancelling", run.getStatus());
    }


    @Test
    @Order(2)
    void retrieveRun() {
        Run run = service.retrieveRun(threadId, runId);
        assertEquals("create", run.getMetadata().get("action"));
        assertNotNull(run);
    }


    @Test
    @Order(2)
    void listRuns() {
        OpenAiResponse<Run> response = service.listRuns(threadId, new ListSearchParameters());
        List<Run> runs = response.getData();
        assertEquals(1, runs.size());
    }


    @Test
    @Order(4)
    void createThreadAndRun() {
        Run run = service.createThreadAndRun(CreateThreadAndRunRequest.builder()
                .assistantId(assistantId)
                .metadata(Collections.singletonMap("action", "create"))
                .thread(
                        ThreadRequest.builder()
                                .messages(Collections.singletonList(
                                        MessageRequest.builder()
                                                .content("请你检索我提供的文件然后回答问题: 田山歌体裁中的包容性具体体现在什么地方?")
                                                .attachments(Collections.singletonList(
                                                        new Attachment(fileId, Collections.singletonList(new FileSearchTool()))
                                                ))
                                                .build()
                                ))
                                .build()
                )
                .temperature(0D)
                .build());
        assertNotNull(run);
        runThreadId = run.getThreadId();
        runThreadRunId = run.getId();
    }


    @Test
    @Order(5)
    void runStep() {
        Run run;
        do {
            run = service.retrieveRun(runThreadId, runThreadRunId);
            assertEquals(runThreadRunId, run.getId());
        } while (!(run.getStatus().equals("completed")) && !(run.getStatus().equals("failed")));

        List<RunStep> runSteps = service.listRunSteps(runThreadId, runThreadRunId, new ListSearchParameters()).getData();
        assertNotNull(runSteps);
        assertEquals(2, runSteps.size());


        RunStep firstRunStep = service.retrieveRunStep(runThreadId, runThreadRunId, runSteps.get(1).getId());
        assertEquals(assistantId, firstRunStep.getAssistantId());
        assertEquals("tool_calls", firstRunStep.getStepDetails().getType());
        assertEquals("file_search", firstRunStep.getStepDetails().getToolCalls().get(0).getType());

        RunStep secondRunStep = service.retrieveRunStep(runThreadId, runThreadRunId, runSteps.get(0).getId());
        assertEquals(assistantId, secondRunStep.getAssistantId());
        assertEquals("message_creation", secondRunStep.getStepDetails().getType());
    }

    @Test
    @Order(6)
    void modifyRun() {
        Run modifiedRun = service.modifyRun(runThreadId, runThreadRunId, ModifyRunRequest.builder().metadata(Collections.singletonMap("action", "retrieve")).build());
        Run run = service.retrieveRun(runThreadId, runThreadRunId);
        assertEquals("retrieve", run.getMetadata().get("action"));
    }


}
