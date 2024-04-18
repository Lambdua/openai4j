package com.theokanning.openai.messages.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LiangTao
 * @date 2024年04月18 16:42
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TextWrapper {
    /**
     * always text
     */
    String type;

    Text text;

}
