package com.theokanning.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * The OpenAI resources used by a request
 */
@Data
public class Usage {
    /**
     * The number of prompt tokens used.
     */
    @JsonProperty("prompt_tokens")
    long promptTokens;

    /**
     * The number of completion tokens used.
     */
    @JsonProperty("completion_tokens")
    long completionTokens;

    /**
     * The number of total tokens used
     */
    @JsonProperty("total_tokens")
    long totalTokens;

    /**
     * Breakdown of tokens used in the prompt.
     */
    @JsonProperty("prompt_tokens_details")
    PromptTokensDetails promptTokensDetails;

    /**
     * Breakdown of tokens used in a completion.
     */
    @JsonProperty("completion_tokens_details")
    CompletionTokensDetails completionTokensDetails;
}
