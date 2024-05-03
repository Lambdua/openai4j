package com.theokanning.openai.assistants.run;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2023-11-16 22:34
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCallCodeInterpreterOutput {

    private Integer index;

    /**
     * logs/image
     */
    private String type;

    /**
     * Text output from the Code Interpreter tool call as part of a run step.
     */
    private String logs;


    private RunImage image;
}
