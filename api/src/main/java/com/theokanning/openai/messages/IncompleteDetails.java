package com.theokanning.openai.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LiangTao
 * @date 2024年04月18 15:56
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IncompleteDetails {
    /**
     * the reason the message is incomplete
     */
    String reason;
}
