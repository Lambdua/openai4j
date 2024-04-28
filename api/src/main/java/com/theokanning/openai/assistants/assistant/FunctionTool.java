package com.theokanning.openai.assistants.assistant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LiangTao
 * @date 2024年04月18 13:37
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FunctionTool implements Tool {
    final String type = "function";

    /**
     * Function definition, only used if type is "function"
     */
    Object function;
}
