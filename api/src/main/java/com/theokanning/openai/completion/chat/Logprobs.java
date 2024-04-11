package com.theokanning.openai.completion.chat;

import lombok.Data;

import java.util.List;

/**
 * Log probability information for the choice.
 *
 * @author LiangTao
 * @date 2024年04月11 16:50
 **/
@Data
public class Logprobs {
    /**
     * A list of message content tokens with log probability information.
     */
    List<LogprobsContent> content;
}
