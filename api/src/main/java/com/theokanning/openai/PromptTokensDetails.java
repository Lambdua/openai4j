package com.theokanning.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Breakdown of tokens used in the prompt.
 */
@Data
public class PromptTokensDetails {
    /**
     * Cached tokens present in the prompt.
     */
    @JsonProperty("cached_tokens")
    long cachedTokens;

    /**
     * Audio input tokens present in the prompt.
     */
    @JsonProperty("audio_tokens")
    long audioTokens;
}
