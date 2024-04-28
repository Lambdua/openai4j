package com.theokanning.openai.assistants.assistant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2023-11-20 10:09
 **/


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AssistantFunction {

    private String description;

    private String name;

    private Object parameters;

}
