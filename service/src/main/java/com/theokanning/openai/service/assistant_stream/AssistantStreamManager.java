package com.theokanning.openai.service.assistant_stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.OpenAiError;
import com.theokanning.openai.OpenAiHttpException;
import com.theokanning.openai.assistants.StreamEvent;
import com.theokanning.openai.assistants.message.Message;
import com.theokanning.openai.assistants.message.content.MessageDelta;
import com.theokanning.openai.assistants.run.Run;
import com.theokanning.openai.assistants.run.ToolCall;
import com.theokanning.openai.assistants.run.ToolCallFunction;
import com.theokanning.openai.assistants.run_step.RunStep;
import com.theokanning.openai.assistants.run_step.RunStepDelta;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * assistant stream 管理器,帮助处理assistant stream的事件
 * 支持同步/异步处理流
 * 支持事件回调
 *
 * @author LiangTao
 * @date 2024年04月29 14:03
 **/
@Slf4j
public class AssistantStreamManager {
    private final AssistantEventHandler eventHandler;
    private final List<MessageDelta> msgDeltas;
    private final List<RunStepDelta> runStepDeltas;
    private final List<AssistantSSE> eventMsgsHolder;
    private final ObjectMapper mapper = new ObjectMapper();


    @Getter
    private MessageDelta accumulatedMessageDelta;

    @Getter
    private RunStepDelta accumulatedRsd;

    @Getter
    private Run currentRun;

    @Getter
    private Message currentMessage;

    @Getter
    private RunStep currentRunStep;

    @Getter
    private volatile boolean completed;

    private Flowable<AssistantSSE> stream;

    private Disposable disposable;


    /**
     * 一个异步的Assistant stream管理器
     *
     * @param stream 一个AssistantSSE的流
     */
    public AssistantStreamManager(Flowable<AssistantSSE> stream) {
        this(stream, new AssistantEventHandler() {
        });
    }

    /**
     * assistant 流响应处理器
     *
     * @param stream       一个AssistantSSE的流
     * @param eventHandler 事件处理器
     */
    public AssistantStreamManager(Flowable<AssistantSSE> stream, AssistantEventHandler eventHandler) {
        this.eventHandler = eventHandler;
        this.msgDeltas = new ArrayList<>();
        this.runStepDeltas = new ArrayList<>();
        this.eventMsgsHolder = new ArrayList<>();
        this.stream = stream;
    }

    public void start() {
        disposable = stream.subscribe(this::handleEvent, eventHandler::onError, () -> completed = true);
    }

    public void shutdown() {
        if (disposable != null) {
            disposable.dispose();
        }
    }

    public void asyncStart() {
        stream.blockingSubscribe(this::handleEvent, eventHandler::onError, () -> completed = true);
    }

    private void handleEvent(AssistantSSE sse) {
        StreamEvent eventType = sse.getEvent();
        eventMsgsHolder.add(sse);
        eventHandler.onEvent(sse);
        try {
            switch (eventType) {
                case THREAD_RUN_CREATED:
                    updateCurrentRun(sse);
                    eventHandler.onRunCreated(currentRun);
                    break;
                case THREAD_RUN_QUEUED:
                    updateCurrentRun(sse);
                    eventHandler.onRunQueued(currentRun);
                    break;
                case THREAD_RUN_IN_PROGRESS:
                    updateCurrentRun(sse);
                    eventHandler.onRunInProgress(currentRun);
                    break;
                case THREAD_RUN_REQUIRES_ACTION:
                    updateCurrentRun(sse);
                    translationRunStepDelta();
                    eventHandler.onRunRequiresAction(currentRun);
                    break;
                case THREAD_RUN_COMPLETED:
                    updateCurrentRun(sse);
                    eventHandler.onRunCompleted(currentRun);
                    break;
                case THREAD_RUN_FAILED:
                    updateCurrentRun(sse);
                    log.warn("run:{} failed at:{}", currentRun.getId(), currentRun.getFailedAt());
                    eventHandler.onRunFailed(currentRun);
                    break;
                case THREAD_RUN_CANCELLING:
                    updateCurrentRun(sse);
                    eventHandler.onRunCancelling(currentRun);
                    break;
                case THREAD_RUN_CANCELLED:
                    updateCurrentRun(sse);
                    eventHandler.onRunCancelled(currentRun);
                    break;
                case THREAD_RUN_EXPIRED:
                    updateCurrentRun(sse);
                    log.warn("run:{} expired at:{}", currentRun.getId(), currentRun.getExpiresAt());
                    eventHandler.onRunExpired(currentRun);
                    break;
                case THREAD_RUN_STEP_CREATED:
                    updateCurrentRunStep(sse);
                    eventHandler.onRunStepCreated(currentRunStep);
                    break;
                case THREAD_RUN_STEP_IN_PROGRESS:
                    updateCurrentRunStep(sse);
                    eventHandler.onRunStepInProgress(currentRunStep);
                    break;
                case THREAD_RUN_STEP_DELTA:
                    accumulateRunStepDeltaAndSave(sse);
                    eventHandler.onRunStepDelta(this.runStepDeltas.get(runStepDeltas.size() - 1));
                    break;
                case THREAD_RUN_STEP_COMPLETED:
                    updateCurrentRunStep(sse);
                    eventHandler.onRunStepCompleted(currentRunStep);
                    break;
                case THREAD_RUN_STEP_FAILED:
                    updateCurrentRunStep(sse);
                    log.warn("runid:{} ,RunStepId:{} failed at:{}", currentRun.getId(), currentRunStep.getId(), currentRunStep.getFailedAt());
                    eventHandler.onRunStepFailed(currentRunStep);
                    break;
                case THREAD_RUN_STEP_CANCELLED:
                    updateCurrentRunStep(sse);
                    eventHandler.onRunStepCancelled(currentRunStep);
                    break;
                case THREAD_RUN_STEP_EXPIRED:
                    updateCurrentRunStep(sse);
                    log.warn("runid:{} ,RunStepId:{} expired at: {}", currentRun.getId(), currentRunStep.getId(), currentRunStep.getExpiredAt());
                    eventHandler.onRunStepExpired(currentRunStep);
                    break;
                case THREAD_MESSAGE_CREATED:
                    updateCurrentMessage(sse);
                    eventHandler.onMessageCreated(currentMessage);
                    break;
                case THREAD_MESSAGE_IN_PROGRESS:
                    updateCurrentMessage(sse);
                    eventHandler.onMessageInProgress(currentMessage);
                    break;
                case THREAD_MESSAGE_DELTA:
                    accumulateMessageDeltaAndSave(sse);
                    eventHandler.onMessageDelta(this.msgDeltas.get(msgDeltas.size() - 1));
                    break;
                case THREAD_MESSAGE_COMPLETED:
                    updateCurrentMessage(sse);
                    eventHandler.onMessageCompleted(currentMessage);
                    break;
                case THREAD_MESSAGE_INCOMPLETE:
                    updateCurrentMessage(sse);
                    log.warn("Message:{} incomplete", currentMessage.getId());
                    eventHandler.onMessageInComplete(currentMessage);
                    break;
                case DONE:
                    completed = true;
                    eventHandler.onEnd();
                    break;
                case ERROR:
                    log.error("Stream error,the final message is:{},Run is {} ", currentMessage, currentRun);
                    completed = true;
                    eventHandler.onError(new OpenAiHttpException(mapper.readValue(sse.getData(), OpenAiError.class), null, 200));
                    break;
            }
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException", e);
            completed = true;
            eventHandler.onError(e);
        }
    }

    public void waitForCompletion() {
        while (!completed) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error("InterruptedException", e);
                shutdown();
            }
        }
    }

    public StreamEvent getCurrentEvent() {
        return eventMsgsHolder.isEmpty() ? null : eventMsgsHolder.get(eventMsgsHolder.size() - 1).getEvent();
    }

    /**
     * 返回sse事件流,这里返回的是一个新的list,不会影响内部的list
     */
    public List<AssistantSSE> getEventMsgsHolder() {
        return new ArrayList<>(eventMsgsHolder);
    }


    public List<MessageDelta> getMsgDeltas() {
        return new ArrayList<>(msgDeltas);
    }

    public List<RunStepDelta> getRunStepDeltas() {
        return new ArrayList<>(runStepDeltas);
    }


    /**
     * 将之前合并的string类型的json function参数转换为jsonNode
     */
    private void translationRunStepDelta() throws JsonProcessingException {
        for (ToolCall toolCall : accumulatedRsd.getDelta().getStepDetails().getToolCalls()) {
            ToolCallFunction function = toolCall.getFunction();
            function.setArguments(mapper.readTree(function.getArguments().asText()));
        }
    }


    private void updateCurrentRunStep(AssistantSSE sse) throws JsonProcessingException {
        if (!sse.getEvent().dataClass.equals(RunStep.class)) {
            throw new IllegalArgumentException("Event data is not a RunStep,raw data is: " + sse.getData() + "event is:" + sse.getEvent().name());
        }
        this.currentRunStep = mapper.readValue(sse.getData(), RunStep.class);
    }

    private void updateCurrentRun(AssistantSSE sse) throws JsonProcessingException {
        if (!sse.getEvent().dataClass.equals(Run.class)) {
            throw new IllegalArgumentException("Event data is not a Run,raw data is: " + sse.getData() + "event is:" + sse.getEvent().name());
        }
        this.currentRun = mapper.readValue(sse.getData(), Run.class);
    }

    private void updateCurrentMessage(AssistantSSE sse) throws JsonProcessingException {
        if (!sse.getEvent().dataClass.equals(Message.class)) {
            throw new IllegalArgumentException("Event data is not a Message,raw data is: " + sse.getData() + "event is:" + sse.getEvent().name());
        }
        this.currentMessage = mapper.readValue(sse.getData(), Message.class);
    }


    private void accumulateRunStepDeltaAndSave(AssistantSSE sse) throws JsonProcessingException {
        if (!sse.getEvent().dataClass.equals(RunStepDelta.class)) {
            throw new IllegalArgumentException("Event data is not a RunStepDelta,raw data is: " + sse.getData() + "event is:" + sse.getEvent().name());
        }
        RunStepDelta currentRenStepDelta = mapper.readValue(sse.getData(), RunStepDelta.class);
        this.runStepDeltas.add(currentRenStepDelta);
        accumulatedRsd = DeltaUtil.accumulatRunStepDelta(accumulatedRsd, currentRenStepDelta);
    }


    private void accumulateMessageDeltaAndSave(AssistantSSE sse) throws JsonProcessingException {
        if (!sse.getEvent().dataClass.equals(MessageDelta.class)) {
            throw new IllegalArgumentException("Event data is not a MessageDelta,raw data is: " + sse.getData() + "event is:" + sse.getEvent().name());
        }
        MessageDelta msgDelta = mapper.readValue(sse.getData(), MessageDelta.class);
        this.msgDeltas.add(msgDelta);
        accumulatedMessageDelta = DeltaUtil.accumulatMessageDelta(accumulatedMessageDelta, msgDelta);
    }

}
