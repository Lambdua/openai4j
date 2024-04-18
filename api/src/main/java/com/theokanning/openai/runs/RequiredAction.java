package com.theokanning.openai.runs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2023-11-16 22:44
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequiredAction {

    /**
     * For now, this is always submit_tool_outputs.
     */
    private String type;

    @JsonProperty("submit_tool_outputs")
    private SubmitToolOutputs submitToolOutputs;
}
