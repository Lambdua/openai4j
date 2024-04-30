package com.theokanning.openai.service.assistant_stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.theokanning.openai.OpenAiError;
import com.theokanning.openai.OpenAiHttpException;
import com.theokanning.openai.assistants.StreamEvent;
import com.theokanning.openai.assistants.message.Message;
import com.theokanning.openai.assistants.message.content.Delta;
import com.theokanning.openai.assistants.message.content.DeltaContent;
import com.theokanning.openai.assistants.message.content.MessageDelta;
import com.theokanning.openai.assistants.message.content.Text;
import com.theokanning.openai.assistants.run.Run;
import com.theokanning.openai.assistants.run.ToolCall;
import com.theokanning.openai.assistants.run.ToolCallFunction;
import com.theokanning.openai.assistants.run_step.RunStep;
import com.theokanning.openai.assistants.run_step.RunStepDelta;
import com.theokanning.openai.assistants.run_step.StepDetails;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
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
    private Run currentRun;
    private Message currentMessage;
    private RunStep currentRunStep;
    private MessageDelta accumulatedMessageDelta;
    private RunStepDelta accumulatedRsd;
    private boolean completed = false;


    public AssistantStreamManager(Flowable<AssistantSSE> stream) {
        this(stream, new AssistantEventHandler() {
        }, false);
    }

    public AssistantStreamManager(Flowable<AssistantSSE> stream, boolean isAsync) {
        this(stream, new AssistantEventHandler() {
        }, isAsync);
    }


    public AssistantStreamManager(Flowable<AssistantSSE> stream, AssistantEventHandler eventHandler, boolean isAsync) {
        this.eventHandler = eventHandler;
        this.msgDeltas = new ArrayList<>();
        this.runStepDeltas = new ArrayList<>();
        this.eventMsgsHolder = new ArrayList<>();
        if (isAsync) {
            stream.subscribe(this::handleEvent, this::handleError);
        } else {
            stream.blockingSubscribe(this::handleEvent, this::handleError);
        }
    }

    private void handleError(Throwable throwable) {
        if (throwable instanceof OpenAiHttpException) {
            OpenAiHttpException exception = (OpenAiHttpException) throwable;
            eventHandler.onError(new OpenAiError(new OpenAiError.OpenAiErrorDetails(exception.getMessage(), exception.type, exception.param, exception.code)));
        } else {
            log.error("assistant stream Unknown error", throwable);
            eventHandler.onError(new OpenAiError(new OpenAiError.OpenAiErrorDetails(throwable.getMessage(), "unknown", "unknown", "unknown")));
        }
    }

    private void handleEvent(AssistantSSE sse) throws JsonProcessingException {
        StreamEvent eventType = sse.getEvent();
        eventMsgsHolder.add(0, sse);
        eventHandler.onEvent(sse);
        switch (eventType) {
            case THREAD_RUN_CREATED:
                updateCurrentRun(sse);
                log.debug("run:{} created", currentRun.getId());
                eventHandler.onRunCreated(currentRun);
                break;
            case THREAD_RUN_QUEUED:
                updateCurrentRun(sse);
                log.debug("run:{} queued", currentRun.getId());
                eventHandler.onRunQueued(currentRun);
                break;
            case THREAD_RUN_IN_PROGRESS:
                updateCurrentRun(sse);
                log.debug("run:{} in progress", currentRun.getId());
                eventHandler.onRunInProgress(currentRun);
                break;
            case THREAD_RUN_REQUIRES_ACTION:
                updateCurrentRun(sse);
                log.debug("run:{} requires action", currentRun.getId());
                translationRunStepDelta();
                eventHandler.onRunRequiresAction(currentRun);
                break;
            case THREAD_RUN_COMPLETED:
                updateCurrentRun(sse);
                log.debug("run:{} completed", currentRun.getId());
                eventHandler.onRunCompleted(currentRun);
                break;
            case THREAD_RUN_FAILED:
                updateCurrentRun(sse);
                log.error("run:{} failed at:{}", currentRun.getId(), currentRun.getFailedAt());
                eventHandler.onRunFailed(currentRun);
                break;
            case THREAD_RUN_CANCELLING:
                updateCurrentRun(sse);
                log.debug("run:{} cancelling", currentRun.getId());
                eventHandler.onRunCancelling(currentRun);
                break;
            case THREAD_RUN_CANCELLED:
                updateCurrentRun(sse);
                log.debug("run:{} cancelled", currentRun.getId());
                eventHandler.onRunCancelled(currentRun);
                break;
            case THREAD_RUN_EXPIRED:
                updateCurrentRun(sse);
                log.warn("run:{} expired at:{}", currentRun.getId(), currentRun.getExpiresAt());
                eventHandler.onRunExpired(currentRun);
                break;
            case THREAD_RUN_STEP_CREATED:
                updateCurrentRunStep(sse);
                log.debug("runid:{} ,RunStepId:{} created", currentRun.getId(), currentRunStep.getId());
                eventHandler.onRunStepCreated(currentRunStep);
                break;
            case THREAD_RUN_STEP_IN_PROGRESS:
                updateCurrentRunStep(sse);
                log.debug("runid:{} ,RunStepId:{} in progress", currentRun.getId(), currentRunStep.getId());
                eventHandler.onRunStepInProgress(currentRunStep);
                break;
            case THREAD_RUN_STEP_DELTA:
                accumulateRunStepDeltaAndSave(sse);
                eventHandler.onRunStepDelta(this.runStepDeltas.get(0));
                break;
            case THREAD_RUN_STEP_COMPLETED:
                updateCurrentRunStep(sse);
                log.debug("runid:{} ,RunStepId:{} completed", currentRun.getId(), currentRunStep.getId());
                eventHandler.onRunStepCompleted(currentRunStep);
                break;
            case THREAD_RUN_STEP_FAILED:
                updateCurrentRunStep(sse);
                log.error("runid:{} ,RunStepId:{} failed at:{}", currentRun.getId(), currentRunStep.getId(), currentRunStep.getFailedAt());
                eventHandler.onRunStepFailed(currentRunStep);
                break;
            case THREAD_RUN_STEP_CANCELLED:
                updateCurrentRunStep(sse);
                log.debug("runid:{} ,RunStepId:{} cancelled at:{}", currentRun.getId(), currentRunStep.getId(), currentRunStep.getCancelledAt());
                eventHandler.onRunStepCancelled(currentRunStep);
                break;
            case THREAD_RUN_STEP_EXPIRED:
                updateCurrentRunStep(sse);
                log.warn("runid:{} ,RunStepId:{} expired at: {}", currentRun.getId(), currentRunStep.getId(), currentRunStep.getExpiredAt());
                eventHandler.onRunStepExpired(currentRunStep);
                break;
            case THREAD_MESSAGE_CREATED:
                updateCurrentMessage(sse);
                log.debug("Message:{} created", currentMessage.getId());
                eventHandler.onMessageCreated(currentMessage);
                break;
            case THREAD_MESSAGE_IN_PROGRESS:
                updateCurrentMessage(sse);
                log.debug("Message:{} in progress", currentMessage.getId());
                eventHandler.onMessageInProgress(currentMessage);
                break;
            case THREAD_MESSAGE_DELTA:
                accumulateMessageDeltaAndSave(sse);
                eventHandler.onMessageDelta(this.msgDeltas.get(0));
                break;
            case THREAD_MESSAGE_COMPLETED:
                updateCurrentMessage(sse);
                log.debug("Message:{} completed", currentMessage.getId());
                eventHandler.onMessageCompleted(currentMessage);
                break;
            case THREAD_MESSAGE_INCOMPLETE:
                updateCurrentMessage(sse);
                log.warn("Message:{} incomplete", currentMessage.getId());
                eventHandler.onMessageInComplete(currentMessage);
                break;
            case DONE:
                log.debug("Stream done,the final message is:{},Run is {} ", currentMessage, currentRun);
                eventHandler.onEnd();
                completed = true;
                break;
            case ERROR:
                log.error("Stream error,the final message is:{},Run is {} ", currentMessage, currentRun);
                eventHandler.onError(mapper.readValue(sse.getData(), OpenAiError.class));
                break;
        }
    }

    public boolean isCompleted() {
        return completed;
    }

    public Message getCurrentMessage() {
        return currentMessage;
    }

    public Run getCurrentRun() {
        return currentRun;
    }

    public RunStep getCurrentRunStep() {
        return currentRunStep;
    }

    public StreamEvent getCurrentEvent() {
        return eventMsgsHolder.get(0).getEvent();
    }

    public List<AssistantSSE> getEventMsgsHolder() {
        return eventMsgsHolder;
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

    /**
     * 累加RunStepDelta
     */
    private void accumulateRunStepDeltaAndSave(AssistantSSE sse) throws JsonProcessingException {
        if (!sse.getEvent().dataClass.equals(RunStepDelta.class)) {
            throw new IllegalArgumentException("Event data is not a RunStepDelta");
        }
        RunStepDelta currentRenStepDelta = mapper.readValue(sse.getData(), RunStepDelta.class);
        this.runStepDeltas.add(0, currentRenStepDelta);
        accumulatedRunStepDelta(currentRenStepDelta);
    }


    /**
     * 更新当前RunStep
     */
    private void updateCurrentRunStep(AssistantSSE sse) throws JsonProcessingException {
        if (!sse.getEvent().dataClass.equals(RunStep.class)) {
            throw new IllegalArgumentException("Event data is not a RunStep");
        }
        this.currentRunStep = mapper.readValue(sse.getData(), RunStep.class);
    }

    private void updateCurrentRun(AssistantSSE sse) throws JsonProcessingException {
        if (!sse.getEvent().dataClass.equals(Run.class)) {
            throw new IllegalArgumentException("Event data is not a Run");
        }
        this.currentRun = mapper.readValue(sse.getData(), Run.class);
    }

    private void updateCurrentMessage(AssistantSSE sse) throws JsonProcessingException {
        if (!sse.getEvent().dataClass.equals(Message.class)) {
            throw new IllegalArgumentException("Event data is not a Message");
        }
        this.currentMessage = mapper.readValue(sse.getData(), Message.class);
    }

    /**
     * 累加MessageDelta并将历史记录合并
     *
     * @author liangtao
     * @date 2024/4/29
     **/
    private void accumulateMessageDeltaAndSave(AssistantSSE sse) throws JsonProcessingException {
        if (!sse.getEvent().dataClass.equals(MessageDelta.class)) {
            throw new IllegalArgumentException("Event data is not a MessageDelta");
        }
        MessageDelta msgDelta = mapper.readValue(sse.getData(), MessageDelta.class);
        this.msgDeltas.add(0, msgDelta);
        accumulatedMessageDelta(msgDelta);
    }

    private void accumulatedRunStepDelta(RunStepDelta currentRenStepDelta) {
        if (this.accumulatedRsd == null) {
            this.accumulatedRsd = currentRenStepDelta;
        } else {
            StepDetails currentDetails = currentRenStepDelta.getDelta().getStepDetails();
            ToolCall currentToolCallPart = currentDetails.getToolCalls().get(0);
            StepDetails preDetails = accumulatedRsd.getDelta().getStepDetails();
            Optional<ToolCall> existsToolCallOptional = preDetails.getToolCalls().stream().filter(t -> t.getIndex().equals(currentToolCallPart.getIndex())).findFirst();
            if (!existsToolCallOptional.isPresent()) {
                preDetails.setToolCalls(currentDetails.getToolCalls());
                return;
            }
            ToolCall existsToolCallPart = existsToolCallOptional.get();
            if (existsToolCallPart.getType().equals("function")) {
                ToolCallFunction currentFunPart = currentToolCallPart.getFunction();
                ToolCallFunction existsFunPart = existsToolCallPart.getFunction();
                if (currentFunPart.getName() != null && !currentFunPart.getName().isEmpty()) {
                    existsFunPart.setName(Optional.ofNullable(existsFunPart.getName()).orElse("") + currentFunPart.getName());
                }
                if (currentFunPart.getArguments() != null) {
                    existsFunPart.setArguments(new TextNode(Optional.ofNullable(existsFunPart.getArguments()).orElse(new TextNode("")).asText() + currentFunPart.getArguments().asText()));
                }
            } else if (existsToolCallPart.getType().equals("file_search")) {

                //todo 合并code_interpreter和file_search类型的数据
            } else if (existsToolCallPart.getType().equals("code_interpreter")) {

                //todo 合并code_interpreter和file_search类型的数据
            }
        }


    }


    private void accumulatedMessageDelta(MessageDelta messageDelta) {
        if (this.accumulatedMessageDelta == null) {
            this.accumulatedMessageDelta = messageDelta;
        } else {
            //merge
            DeltaContent currentDeltaContent = messageDelta.getDelta().getContent().get(0);
            Delta preDelta = accumulatedMessageDelta.getDelta();
            List<DeltaContent> preContent = preDelta.getContent();
            Optional<DeltaContent> existsCurrent = preContent.stream().filter(c -> c.getIndex().equals(currentDeltaContent.getIndex())).findFirst();
            if (existsCurrent.isPresent()) {
                DeltaContent existsContent = existsCurrent.get();
                if (!currentDeltaContent.getType().equals(existsContent.getType())) {
                    throw new IllegalStateException("DeltaContent type is not same");
                }
                if (currentDeltaContent.getType().equals("text")) {
                    Text text = existsContent.getText();
                    text.setValue(text.getValue() + currentDeltaContent.getText().getValue());
                    text.setAnnotations(currentDeltaContent.getText().getAnnotations());
                }
                if (currentDeltaContent.getType().equals("image_file") && currentDeltaContent.getImageFile() != null) {
                    existsContent.setImageFile(currentDeltaContent.getImageFile());
                }
            } else {
                preContent.add(currentDeltaContent);
            }
        }
    }

}
