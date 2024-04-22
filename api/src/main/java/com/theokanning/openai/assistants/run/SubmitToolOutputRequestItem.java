package com.theokanning.openai.assistants.run;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2023-11-16 22:45
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitToolOutputRequestItem {

    /**
     * The ID of the tool call in the required_action object within the run object the output is being submitted for.
     */
    @JsonProperty("tool_call_id")
    private String toolCallId;

    /**
     * The output of the tool call to be submitted to continue the run.
     */
    private String output;
}
