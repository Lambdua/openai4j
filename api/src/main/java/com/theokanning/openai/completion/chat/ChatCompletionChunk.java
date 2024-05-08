package com.theokanning.openai.completion.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.theokanning.openai.Usage;
import lombok.Data;

import java.util.List;

/**
 * Object containing a response chunk from the chat completions streaming api.
 */
@Data
public class ChatCompletionChunk {
	/**
     * Unique id assigned to this chat completion.
     */
    String id;

    /**
     * The type of object returned, should be "chat.completion.chunk"
     */
    String object;

    /**
     * The creation time in epoch seconds.
     */
    long created;

    /**
     * The model used.
     */
    String model;

    /**
     * This fingerprint represents the backend configuration that the model runs with.
     * <p>
     * Can be used in conjunction with the seed request parameter to understand when backend changes have been made that might impact determinism.
     */
    @JsonProperty("system_fingerprint")
    String systemFingerprint;

    /**
     * A list of chat completion choices. Can contain more than one elements if n is greater than 1.
     * Can also be empty for the last chunk if you set stream_options: {"include_usage": true} {@link ChatCompletionRequest#streamOptions}.
     */
    List<ChatCompletionChoice> choices;

    /**
     * An optional field that will only be present when you set stream_options: {"include_usage": true} {@link ChatCompletionRequest#streamOptions} in your request.
     * When present, it contains a null value except for the last chunk which contains the token usage statistics for the entire request.
     */
    Usage usage;

}
