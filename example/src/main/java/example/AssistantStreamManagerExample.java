package example;

import com.theokanning.openai.assistants.assistant.Assistant;
import com.theokanning.openai.assistants.assistant.AssistantRequest;
import com.theokanning.openai.assistants.assistant.FunctionTool;
import com.theokanning.openai.assistants.message.Message;
import com.theokanning.openai.assistants.message.MessageRequest;
import com.theokanning.openai.assistants.message.content.MessageDelta;
import com.theokanning.openai.assistants.run.Run;
import com.theokanning.openai.assistants.run.RunCreateRequest;
import com.theokanning.openai.assistants.run.ToolChoice;
import com.theokanning.openai.assistants.thread.Thread;
import com.theokanning.openai.assistants.thread.ThreadRequest;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.service.assistant_stream.AssistantEventHandler;
import com.theokanning.openai.service.assistant_stream.AssistantSSE;
import com.theokanning.openai.service.assistant_stream.AssistantStreamManager;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author LiangTao
 * @date 2024年05月06 14:39
 **/
public class AssistantStreamManagerExample {


    public static void main(String[] args) {
        streamTest();
    }

    static void streamTest() {
        OpenAiService service = new OpenAiService();

        //1. create assistant
        AssistantRequest assistantRequest = AssistantRequest.builder()
                .model("gpt-3.5-turbo").name("weather assistant")
                .instructions("You are a weather assistant responsible for calling the weather API to return weather information based on the location entered by the user")
                .tools(Collections.singletonList(new FunctionTool(ToolUtil.weatherFunction())))
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
                .content("What can you help me with?")
                .build();
        service.createMessage(threadId, messageRequest);
        RunCreateRequest runCreateRequest = RunCreateRequest.builder()
                .assistantId(assistantId)
                .toolChoice(ToolChoice.AUTO)
                .build();

        //blocking
        // AssistantStreamManager blockedManagere = AssistantStreamManager.syncStart(service.createRunStream(threadId, runCreateRequest), new LogHandler());
        //async
        AssistantStreamManager streamManager = AssistantStreamManager.start(service.createRunStream(threadId, runCreateRequest), new LogHandler());


        //Other operations can be performed here...
        boolean completed = streamManager.isCompleted();


        // you can shut down the streamManager if you want to stop the stream
        streamManager.shutDown();

        //waiting for completion
        streamManager.waitForCompletion();
        // all of flowable events
        List<AssistantSSE> eventMsgsHolder = streamManager.getEventMsgsHolder();

        Optional<Run> currentRun = streamManager.getCurrentRun();
        // get the accumulated message
        streamManager.getAccumulatedMsg().ifPresent(msg -> {
            System.out.println("accumulatedMsg:" + msg);
        });
        service.deleteAssistant(assistantId);
        service.deleteThread(threadId);
    }

    /**
     * 你可以根据自己的需求实现AssistantEventHandler的各个事件回调,方便你处理assistant的各种事件
     */
    private static class LogHandler implements AssistantEventHandler {
        @Override
        public void onEvent(AssistantSSE sse) {
            //每一个事件都会调用这个方法
        }

        @Override
        public void onRunCreated(Run run) {
            System.out.println("start run: " + run.getId());
        }

        @Override
        public void onEnd() {
            System.out.println("stream end");
        }

        @Override
        public void onMessageDelta(MessageDelta messageDelta) {
            System.out.println(messageDelta.getDelta().getContent().get(0).getText());
        }

        @Override
        public void onMessageCompleted(Message message) {
            System.out.println("message completed");
        }

        @Override
        public void onMessageInComplete(Message message) {
            System.out.println("message in complete");
        }

        @Override
        public void onError(Throwable error) {
            System.out.println("error:" + error.getMessage());
        }
    }
}
