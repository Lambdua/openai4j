package com.theokanning.openai.assistants.run;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LiangTao
 * @date 2024年04月18 17:18
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ToolChoice {
    /**
     * The type of the tool. If type is function, the function name must be set
     */
    String type;

    /**
     * The name of the function to call.
     */
    Function function;

}
