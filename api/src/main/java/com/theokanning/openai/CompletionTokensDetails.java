package com.theokanning.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Breakdown of tokens used in a completion.
 */
@Data
public class CompletionTokensDetails {

    @JsonProperty("reasoning_tokens")
    long reasoningTokens;

    @JsonProperty("audio_tokens")
    long audioTokens;

    @JsonProperty("accepted_prediction_tokens")
    long acceptedPredictionTokens;

    @JsonProperty("rejected_prediction_tokens")
    long rejectedPredictionTokens;
}
