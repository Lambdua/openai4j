package example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.ListSearchParameters;
import com.theokanning.openai.OpenAiResponse;
import com.theokanning.openai.assistants.StreamEvent;
import com.theokanning.openai.assistants.assistant.*;
import com.theokanning.openai.assistants.message.Message;
import com.theokanning.openai.assistants.message.MessageRequest;
import com.theokanning.openai.assistants.run.*;
import com.theokanning.openai.assistants.run_step.RunStep;
import com.theokanning.openai.assistants.thread.Attachment;
import com.theokanning.openai.assistants.thread.Thread;
import com.theokanning.openai.assistants.thread.ThreadRequest;
import com.theokanning.openai.file.File;
import com.theokanning.openai.service.FunctionExecutor;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.service.assistant_stream.AssistantSSE;
import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author LiangTao
 * @date 2024年04月30 13:30
 **/
public class AssistantExample {
    public static void main(String[] args) throws JsonProcessingException, UnsupportedEncodingException {
        // assistantToolCall();
        // assistantStream();
        // fileSearchExample();
        codeInterpreterExample();

    }

    static void assistantToolCall() {
        OpenAiService service = new OpenAiService();
        FunctionExecutor executor = new FunctionExecutor(Collections.singletonList(ToolUtil.weatherFunction()));
        AssistantRequest assistantRequest = AssistantRequest.builder()
                .model("gpt-3.5-turbo").name("weather assistant")
                .instructions("You are a weather assistant responsible for calling the weather API to return weather information based on the location entered by the user")
                .tools(Collections.singletonList(new FunctionTool(ToolUtil.weatherFunction())))
                .temperature(0D)
                .build();
        Assistant assistant = service.createAssistant(assistantRequest);
        String assistantId = assistant.getId();
        ThreadRequest threadRequest = ThreadRequest.builder().build();
        Thread thread = service.createThread(threadRequest);
        String threadId = thread.getId();

        MessageRequest messageRequest = MessageRequest.builder()
                .content("What's the weather of Xiamen?")
                .build();
        //add message to thread
        service.createMessage(threadId, messageRequest);
        RunCreateRequest runCreateRequest = RunCreateRequest.builder().assistantId(assistantId).build();

        Run run = service.createRun(threadId, runCreateRequest);

        Run retrievedRun = service.retrieveRun(threadId, run.getId());
        while (!(retrievedRun.getStatus().equals("completed"))
                && !(retrievedRun.getStatus().equals("failed"))
                && !(retrievedRun.getStatus().equals("expired"))
                && !(retrievedRun.getStatus().equals("incomplete"))
                && !(retrievedRun.getStatus().equals("requires_action"))) {
            retrievedRun = service.retrieveRun(threadId, run.getId());
        }
        System.out.println(retrievedRun);

        RequiredAction requiredAction = retrievedRun.getRequiredAction();
        List<ToolCall> toolCalls = requiredAction.getSubmitToolOutputs().getToolCalls();
        ToolCall toolCall = toolCalls.get(0);
        ToolCallFunction function = toolCall.getFunction();
        String toolCallId = toolCall.getId();

        SubmitToolOutputsRequest submitToolOutputsRequest = SubmitToolOutputsRequest.ofSingletonToolOutput(toolCallId, executor.executeAndConvertToJson(function).toPrettyString());
        retrievedRun = service.submitToolOutputs(threadId, retrievedRun.getId(), submitToolOutputsRequest);

        while (!(retrievedRun.getStatus().equals("completed"))
                && !(retrievedRun.getStatus().equals("failed"))
                && !(retrievedRun.getStatus().equals("expired"))
                && !(retrievedRun.getStatus().equals("incomplete"))
                && !(retrievedRun.getStatus().equals("requires_action"))) {
            retrievedRun = service.retrieveRun(threadId, run.getId());
        }

        System.out.println(retrievedRun);

        OpenAiResponse<Message> response = service.listMessages(threadId, new ListSearchParameters());
        List<Message> messages = response.getData();
        messages.forEach(message -> {
            System.out.println(message.getContent());
        });

    }

    static void assistantStream() throws JsonProcessingException {
        OpenAiService service = new OpenAiService();
        String assistantId;
        String threadId;

        AssistantRequest assistantRequest = AssistantRequest.builder()
                .model("gpt-3.5-turbo").name("weather assistant")
                .instructions("You are a weather assistant responsible for calling the weather API to return weather information based on the location entered by the user")
                .tools(Collections.singletonList(new FunctionTool(ToolUtil.weatherFunction())))
                .temperature(0D)
                .build();
        Assistant assistant = service.createAssistant(assistantRequest);
        assistantId = assistant.getId();

        //一般响应
        Flowable<AssistantSSE> threadAndRunStream = service.createThreadAndRunStream(
                CreateThreadAndRunRequest.builder()
                        .assistantId(assistantId)
                        //这里不使用任何工具
                        .toolChoice(ToolChoice.NONE)
                        .thread(ThreadRequest.builder()
                                .messages(Collections.singletonList(
                                        MessageRequest.builder()
                                                .content("你好,你可以帮助我做什么?")
                                                .build()
                                ))
                                .build())
                        .build()
        );

        ObjectMapper objectMapper = new ObjectMapper();

        TestSubscriber<AssistantSSE> subscriber1 = new TestSubscriber<>();
        threadAndRunStream
                .doOnNext(System.out::println)
                .blockingSubscribe(subscriber1);

        Optional<AssistantSSE> runStepCompletion = subscriber1.values().stream().filter(item -> item.getEvent().equals(StreamEvent.THREAD_RUN_STEP_COMPLETED)).findFirst();
        RunStep runStep = objectMapper.readValue(runStepCompletion.get().getData(), RunStep.class);
        System.out.println(runStep.getStepDetails());


        // 函数调用 stream
        threadId = runStep.getThreadId();
        service.createMessage(threadId, MessageRequest.builder().content("请帮我查询北京天气").build());
        Flowable<AssistantSSE> getWeatherFlowable = service.createRunStream(threadId, RunCreateRequest.builder()
                //这里强制使用get_weather函数
                .assistantId(assistantId)
                .toolChoice(new ToolChoice(new Function("get_weather")))
                .build()
        );

        TestSubscriber<AssistantSSE> subscriber2 = new TestSubscriber<>();
        getWeatherFlowable
                .doOnNext(System.out::println)
                .blockingSubscribe(subscriber2);

        AssistantSSE requireActionSse = subscriber2.values().get(subscriber2.values().size() - 2);
        Run requireActionRun = objectMapper.readValue(requireActionSse.getData(), Run.class);
        RequiredAction requiredAction = requireActionRun.getRequiredAction();
        List<ToolCall> toolCalls = requiredAction.getSubmitToolOutputs().getToolCalls();
        ToolCall toolCall = toolCalls.get(0);
        String callId = toolCall.getId();

        System.out.println(toolCall.getFunction());


        // 提交函数调用结果
        Flowable<AssistantSSE> toolCallResponseFlowable = service.submitToolOutputsStream(threadId, requireActionRun.getId(), SubmitToolOutputsRequest.ofSingletonToolOutput(callId, "北京的天气是晴天"));
        TestSubscriber<AssistantSSE> subscriber3 = new TestSubscriber<>();
        toolCallResponseFlowable
                .doOnNext(System.out::println)
                .blockingSubscribe(subscriber3);

        Optional<AssistantSSE> msgSse = subscriber3.values().stream().filter(item -> StreamEvent.THREAD_MESSAGE_COMPLETED.equals(item.getEvent())).findFirst();
        Message message = objectMapper.readValue(msgSse.get().getData(), Message.class);
        String responseContent = message.getContent().get(0).getText().getValue();
        System.out.println(responseContent);
    }

    static void fileSearchExample() throws UnsupportedEncodingException {
        OpenAiService service = new OpenAiService();

        AssistantRequest assistantRequest = AssistantRequest.builder()
                .model("gpt-3.5-turbo")
                .name("file search assistant")
                .instructions("你是一个中国传统音乐教授,负责根据用户的需求解答问题")
                //add file search tool to assistant
                .tools(Collections.singletonList(new FileSearchTool()))
                .temperature(0D)
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
        URL resource = AssistantExample.class.getClassLoader().getResource("田山歌中艺术特征及其共生性特征探析.txt");
        File file = service.uploadFile("assistants", URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8.name()));
        //get resource


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
                .build();
        Run run = service.createRun(threadId, runCreateRequest);
        String runId = run.getId();

        do {
            run = service.retrieveRun(threadId, runId);
        } while (!(run.getStatus().equals("completed")) && !(run.getStatus().equals("failed")));

        List<RunStep> runSteps = service.listRunSteps(threadId, runId, new ListSearchParameters()).getData();

        for (RunStep runStep : runSteps) {
            System.out.println(runStep.getStepDetails());
        }
        service.listMessages(threadId, new ListSearchParameters()).getData().forEach(message -> {
            System.out.println(message.getContent());
        });
    }

    static void codeInterpreterExample() {
        OpenAiService service = new OpenAiService();
        AssistantRequest assistantRequest = AssistantRequest.builder()
                .model("gpt-3.5-turbo")
                .name("code interpreter assistant")
                .instructions("You are a code interpreter assistant.Using code interpreter tools for result calculation")
                .tools(Collections.singletonList(new CodeInterpreterTool()))
                .temperature(0D)
                .build();
        Assistant assistant = service.createAssistant(assistantRequest);
        String assistantId = assistant.getId();
        System.out.println("assistantId:" + assistantId);
        ThreadRequest threadRequest = ThreadRequest.builder()
                .build();
        Thread thread = service.createThread(threadRequest);
        String threadId = thread.getId();
        System.out.println("threadId:" + threadId);
        MessageRequest messageRequest = MessageRequest.builder()
                .content("What does the following value : 5+10*(2^3-2)*1```")
                .build();
        service.createMessage(threadId, messageRequest);
        // RunCreateRequest runCreateRequest = RunCreateRequest.builder()
        //         .assistantId(assistantId)
        //         .toolChoice(ToolChoice.AUTO)
        //         .build();
        // Run run = service.createRun(threadId, runCreateRequest);
        // String runId = run.getId();
        // do {
        //     run = service.retrieveRun(threadId, runId);
        // } while (!(run.getStatus().equals("completed")) && !(run.getStatus().equals("failed")));
        // List<RunStep> runSteps = service.listRunSteps(threadId, runId, new ListSearchParameters()).getData();
        // for (RunStep runStep : runSteps) {
        //     System.out.println(runStep.getStepDetails());
        // }
        // service.listMessages(threadId, new ListSearchParameters()).getData().forEach(message -> {
        //     System.out.println(message.getContent());
        // });
    }


}
