package com.theokanning.openai.assistants.run_step;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private MessageCreation messageCreation;

    @JsonProperty("tool_calls")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ToolCall> toolCalls;

    public String toPrettyString() {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
