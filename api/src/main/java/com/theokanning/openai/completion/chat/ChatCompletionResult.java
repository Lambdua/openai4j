package com.theokanning.openai.completion.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.theokanning.openai.Usage;
import lombok.Data;

import java.util.List;

/**
 * Object containing a response from the chat completions api.
 */
@Data
public class ChatCompletionResult {

    /**
     * Unique id assigned to this chat completion.
     */
    String id;

    /**
     * The type of object returned, should be "chat.completion"
     */
    String object;

    /**
     * The creation time in epoch seconds.
     */
    long created;

    /**
     * The GPT model used.
     */
    String model;

    /**
     * A list of all generated completions.
     */
    List<ChatCompletionChoice> choices;

    /**
     * The API usage for this request.
     */
    Usage usage;

    /**
     * This fingerprint represents the backend configuration that the model runs with.
     * <p>
     * Can be used in conjunction with the seed request parameter to understand when backend changes have been made that might impact determinism.
     */
    @JsonProperty("system_fingerprint")
    String systemFingerprint;

}
