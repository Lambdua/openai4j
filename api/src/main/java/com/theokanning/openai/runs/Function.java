package com.theokanning.openai.runs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LiangTao
 * @date 2024年04月18 17:20
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Function {
    /**
     * The name of the function to call.
     */
    String name;
}
