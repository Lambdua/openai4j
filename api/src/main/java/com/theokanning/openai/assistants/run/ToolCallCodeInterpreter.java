package com.theokanning.openai.assistants.run;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2023-11-16 22:34
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCallCodeInterpreter {

    /**
     * The input to the Code Interpreter tool call.
     */
    private String input;

    /**
     * The outputs from the Code Interpreter tool call.
     * Code Interpreter can output one or more items, including text (logs) or images (image).
     * Each of these are represented by a different object type.
     */
    private List<ToolCallCodeInterpreterOutput> outputs;
}
