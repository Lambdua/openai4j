package com.theokanning.openai.assistants.run;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2023-11-16 22:45
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitToolOutputsRequest {

    Boolean stream;

    /**
     * A list of tools for which the outputs are being submitted.
     */
    @JsonProperty("tool_outputs")
    private List<SubmitToolOutputRequestItem> toolOutputs;


    /**
     * 单个函数调用的请求对象构造
     **/
    public static SubmitToolOutputsRequest ofSingletonToolOutput(String toolCallId, String output) {
        return SubmitToolOutputsRequest.builder()
                .toolOutputs(Collections.singletonList(SubmitToolOutputRequestItem.builder()
                        .toolCallId(toolCallId)
                        .output(output)
                        .build()))
                .build();
    }

}
