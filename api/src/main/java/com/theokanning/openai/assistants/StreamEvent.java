package com.theokanning.openai.assistants;

import com.theokanning.openai.OpenAiError;
import com.theokanning.openai.assistants.message.Message;
import com.theokanning.openai.assistants.message.content.MessageDelta;
import com.theokanning.openai.assistants.run.Run;
import com.theokanning.openai.assistants.run_step.RunStep;
import com.theokanning.openai.assistants.run_step.RunStepDelta;
import com.theokanning.openai.assistants.thread.Thread;
import lombok.AllArgsConstructor;

/**
 * stream events
 *
 * @author LiangTao
 * @date 2024年04月23 16:23
 **/
@AllArgsConstructor
public enum StreamEvent {
    /**
     * Occurs when a new thread is created.
     * data: {@link com.theokanning.openai.assistants.thread.Thread}
     */
    THREAD_CREATED("thread.created", Thread.class),


    /**
     * Occurs when a new run is created.
     * data: {@link com.theokanning.openai.assistants.run.Run}
     */
    THREAD_RUN_CREATED("thread.run.created", Run.class),

    /**
     * Occurs when a run moves to a queued status.
     * data: {@link com.theokanning.openai.assistants.run.Run}
     */
    THREAD_RUN_QUEUED("thread.run.queued", Run.class),

    /**
     * Occurs when a run moves to an in_progress status.
     * data: {@link com.theokanning.openai.assistants.run.Run}
     */
    THREAD_RUN_IN_PROGRESS("thread.run.in_progress", Run.class),

    /**
     * Occurs when a run moves to a requires_action status.
     * data: {@link com.theokanning.openai.assistants.run.Run}
     */
    THREAD_RUN_REQUIRES_ACTION("thread.run.requires_action", Run.class),

    /**
     * Occurs when a run is completed.
     * data: {@link com.theokanning.openai.assistants.run.Run}
     */
    THREAD_RUN_COMPLETED("thread.run.completed", Run.class),

    /**
     * Occurs when a run fails.
     * data: {@link com.theokanning.openai.assistants.run.Run}
     */
    THREAD_RUN_FAILED("thread.run.failed", Run.class),

    /**
     * Occurs when a run moves to a cancelling status.
     * data: {@link com.theokanning.openai.assistants.run.Run}
     */
    THREAD_RUN_CANCELLING("thread.run.cancelling", Run.class),

    /**
     * Occurs when a run is cancelled.
     * data: {@link com.theokanning.openai.assistants.run.Run}
     */
    THREAD_RUN_CANCELLED("thread.run.cancelled", Run.class),

    /**
     * Occurs when a run expires.
     * data: {@link com.theokanning.openai.assistants.run.Run}
     */
    THREAD_RUN_EXPIRED("thread.run.expired", Run.class),

    /**
     * Occurs when a run step is created.
     * data: {@link com.theokanning.openai.assistants.run_step.RunStep}
     */
    THREAD_RUN_STEP_CREATED("thread.run.step.created", RunStep.class),

    /**
     * Occurs when a run step moves to an in_progress state.
     * data: {@link com.theokanning.openai.assistants.run_step.RunStep}
     */
    THREAD_RUN_STEP_IN_PROGRESS("thread.run.step.in_progress", RunStep.class),

    /**
     * Occurs when parts of a run step are being streamed.
     * data: {@link com.theokanning.openai.assistants.run_step.RunStepDelta}
     */
    THREAD_RUN_STEP_DELTA("thread.run.step.delta", RunStepDelta.class),

    /**
     * Occurs when a run step is completed.
     * data: {@link com.theokanning.openai.assistants.run_step.RunStep}
     */
    THREAD_RUN_STEP_COMPLETED("thread.run.step.completed", RunStep.class),

    /**
     * Occurs when a run step fails.
     * data: {@link com.theokanning.openai.assistants.run_step.RunStep}
     */
    THREAD_RUN_STEP_FAILED("thread.run.step.failed", RunStep.class),

    /**
     * Occurs when a run step is cancelled.
     * data: {@link com.theokanning.openai.assistants.run_step.RunStep}
     */
    THREAD_RUN_STEP_CANCELLED("thread.run.step.cancelled", RunStep.class),

    /**
     * Occurs when a run step expires.
     * data: {@link com.theokanning.openai.assistants.run_step.RunStep}
     */
    THREAD_RUN_STEP_EXPIRED("thread.run.step.expired", RunStep.class),

    /**
     * Occurs when a message is created.
     * data: {@link com.theokanning.openai.assistants.message.Message}
     */
    THREAD_MESSAGE_CREATED("thread.message.created", Message.class),

    /**
     * Occurs when a message moves to an in_progress state.
     * data: {@link com.theokanning.openai.assistants.message.Message}
     */
    THREAD_MESSAGE_IN_PROGRESS("thread.message.in_progress", Message.class),

    /**
     * Occurs when parts of a Message are being streamed.
     * data: {@link com.theokanning.openai.assistants.message.content.MessageDelta}
     */
    THREAD_MESSAGE_DELTA("thread.message.delta", MessageDelta.class),

    /**
     * Occurs when a message is completed.
     * data: {@link com.theokanning.openai.assistants.message.Message}
     */
    THREAD_MESSAGE_COMPLETED("thread.message.completed", Message.class),

    /**
     * Occurs when a message ends before it is completed.
     * data: {@link com.theokanning.openai.assistants.message.Message}
     */
    THREAD_MESSAGE_INCOMPLETE("thread.message.incomplete", Message.class),

    /**
     * Occurs when an error occurs. This can happen due to an internal server error or a timeout.
     * data: {@link OpenAiError}
     */
    ERROR("error", OpenAiError.class),

    /**
     * Occurs when a stream ends.
     * data: [DONE]
     */
    DONE("done", Void.class);;

    public String eventName;

    public Class<?> dataClass;


    public static StreamEvent valueByName(String eventName) {
        for (StreamEvent value : values()) {
            if (value.eventName.equals(eventName)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown event name: " + eventName);
    }
}
