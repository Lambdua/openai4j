package com.theokanning.openai.assistants.run_step;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.theokanning.openai.assistants.run.MessageCreation;
import com.theokanning.openai.assistants.run.ToolCall;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepDetails {


    /**
     * message_creation/tool_calls
     */
    private String type;

    @JsonProperty("message_creation")
    private MessageCreation messageCreation;

    @JsonProperty("tool_calls")
    private List<ToolCall> toolCalls;
}
