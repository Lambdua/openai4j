package com.theokanning.openai.service.assistants;

import com.theokanning.openai.ListSearchParameters;
import com.theokanning.openai.assistants.assistant.Assistant;
import com.theokanning.openai.assistants.assistant.AssistantRequest;
import com.theokanning.openai.assistants.assistant.FileSearchTool;
import com.theokanning.openai.assistants.message.MessageListSearchParameters;
import com.theokanning.openai.assistants.message.MessageRequest;
import com.theokanning.openai.assistants.run.Run;
import com.theokanning.openai.assistants.run.RunCreateRequest;
import com.theokanning.openai.assistants.run.ToolChoice;
import com.theokanning.openai.assistants.run_step.RunStep;
import com.theokanning.openai.assistants.thread.Attachment;
import com.theokanning.openai.assistants.thread.Thread;
import com.theokanning.openai.assistants.thread.ThreadRequest;
import com.theokanning.openai.file.File;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.service.assistant_stream.AssistantSSE;
import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author LiangTao
 * @date 2024年08月15 14:49
 **/
public class AssistantFileSearchTest {
    OpenAiService service = new OpenAiService();

    @Test
    void fileSearchExample(){
        OpenAiService service = new OpenAiService();

        AssistantRequest assistantRequest = AssistantRequest.builder()
                .model("gpt-4o-mini")
                .name("file search assistant")
                .instructions("你是一个中国传统音乐教授,负责根据用户的需求解答问题")
                //add file search tool to assistant
                .tools(Collections.singletonList(new FileSearchTool()))
                .temperature(0.3D)
                .build();
        Assistant assistant = service.createAssistant(assistantRequest);
        String assistantId = assistant.getId();
        System.out.println("assistantId:" + assistantId);

        ThreadRequest threadRequest = ThreadRequest.builder()
                .build();
        Thread thread = service.createThread(threadRequest);
        String threadId = thread.getId();
        System.out.println("threadId:" + threadId);

        //upload file for message attachment
        File file = service.uploadFile("assistants", getClass().getClassLoader().getResourceAsStream("田山歌中艺术特征及其共生性特征探析.txt"), "田山歌中艺术特征及其共生性特征探析.txt");

        String fileId = file.getId();
        System.out.println("fileId:" + fileId);

        MessageRequest messageRequest = MessageRequest.builder()
                //query user to search file
                .content("请你检索我提供的文件然后回答问题: 田山歌体裁中的包容性具体体现在什么地方?")
                .attachments(Collections.singletonList(
                        //add uploaded file to message with file search tool
                        new Attachment(fileId, Collections.singletonList(new FileSearchTool()))
                ))
                .build();
        //add msg to thread
        service.createMessage(threadId, messageRequest);

        //run
        RunCreateRequest runCreateRequest = RunCreateRequest.builder()
                .assistantId(assistantId)
                .toolChoice(ToolChoice.AUTO)
                .stream(true)
                .build();

        // Run run = service.createRun(threadId, runCreateRequest);
        Flowable<AssistantSSE> runStream = service.createRunStream(threadId, runCreateRequest);

        TestSubscriber<AssistantSSE> subscriber2 = new TestSubscriber<>();
        runStream.blockingSubscribe(subscriber2);
        subscriber2.assertComplete();
        subscriber2.assertNoErrors();
        assertFalse(subscriber2.values().isEmpty());

        //delete assistant
        service.deleteThread(threadId);
        service.deleteAssistant(assistantId);
        service.deleteFile(fileId);

    }
}
