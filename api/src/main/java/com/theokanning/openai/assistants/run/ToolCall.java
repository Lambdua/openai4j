package com.theokanning.openai.assistants.run;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2023-11-16 22:32
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCall {
    //may be need     @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    Integer index;

    String id;

    String type;

    @JsonProperty("code_interpreter")
    ToolCallCodeInterpreter codeInterpreter;

    @JsonProperty("file_search")
    Map<String, String> fileSearch;

    ToolCallFunction function;
}
