package com.theokanning.openai.service.assistant_stream;

import com.theokanning.openai.OpenAiError;
import com.theokanning.openai.OpenAiHttpException;
import com.theokanning.openai.assistants.message.Message;
import com.theokanning.openai.assistants.message.content.MessageDelta;
import com.theokanning.openai.assistants.run.Run;
import com.theokanning.openai.assistants.run_step.RunStep;
import com.theokanning.openai.assistants.run_step.RunStepDelta;

/**
 * @deprecated 这个类是实验性的，尚未经过充分测试，并且在未来版本中可能会有较大变动。建议在严格测试之前不要在生产环境中使用。将在下个版本中完善,也可能发生较大变动
 * @author LiangTao
 * @date 2024年04月29 11:58
 **/
@Deprecated
public interface AssistantEventHandler {

    default void onEvent(AssistantSSE sse) {

    }

    default void onRunCreated(Run run) {

    }

    default void onRunQueued(Run run) {

    }

    default void onRunInProgress(Run run) {

    }


    default void onRunRequiresAction(Run run) {

    }


    default void onRunCompleted(Run run) {

    }

    default void onRunFailed(Run run) {

    }

    default void onRunCancelled(Run run) {

    }

    default void onRunExpired(Run run) {

    }

    default void onRunStepCreated(RunStep runStep) {

    }

    default void onRunStepInProgress(RunStep runStep) {

    }

    default void onRunStepDelta(RunStepDelta runStepDelta) {

    }


    default void onRunStepCompleted(RunStep runStep) {

    }

    default void onRunStepFailed(RunStep runStep) {

    }

    default void onRunStepCancelled(RunStep runStep) {

    }

    default void onRunStepExpired(RunStep runStep) {

    }

    default void onMessageCreated(Message message) {

    }

    default void onMessageInProgress(Message message) {

    }

    default void onMessageDelta(MessageDelta messageDelta) {

    }

    default void onMessageCompleted(Message message) {

    }

    default void onMessageInComplete(Message message) {

    }

    default void onRunCancelling(Run cancellingRun) {

    }


    default void onEnd() {

    }

    default void onError(OpenAiError error) {
        throw new OpenAiHttpException(error, null, 0);
    }


}
