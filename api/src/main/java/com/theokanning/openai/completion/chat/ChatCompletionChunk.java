package com.theokanning.openai.completion.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
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
     * A list of all generated completions.
     */
    List<ChatCompletionChoice> choices;

}
