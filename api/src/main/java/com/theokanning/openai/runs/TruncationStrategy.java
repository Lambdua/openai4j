package com.theokanning.openai.runs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * @author LiangTao
 * @date 2024年04月18 17:09
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TruncationStrategy {
    /**
     * The truncation strategy to use for the thread. The default is auto.
     * If set to last_messages, the thread will be truncated to the n most recent messages in the thread.
     * When set to auto, messages in the middle of the thread will be dropped to fit the context length of the model, max_prompt_tokens.
     */
    @NonNull
    String type;

    /**
     * The number of most recent messages from the thread when constructing the context for the run.
     */
    @JsonProperty("last_messages")
    Integer lastMessages;

}
