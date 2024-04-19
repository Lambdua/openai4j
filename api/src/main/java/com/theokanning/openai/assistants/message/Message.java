package com.theokanning.openai.assistants.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.theokanning.openai.assistants.thread.Attachment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;


/**
 * Represents a Message within a thread.
 * <p>
 * https://platform.openai.com/docs/api-reference/messages/object
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Message {
    /**
     * The identifier, which can be referenced in API endpoints.
     */
    String id;

    /**
     * The object type, which is always thread.message.
     */
    String object;

    /**
     * The Unix timestamp (in seconds) for when the message was created.
     */
    @JsonProperty("created_at")
    int createdAt;

    /**
     * The thread ID that this message belongs to.
     */
    @JsonProperty("thread_id")
    String threadId;

    /**
     * The status of the message, which can be either in_progress, incomplete, or completed.
     */
    String status;

    /**
     * On an incomplete message, details about why the message is incomplete.
     */
    @JsonProperty("incomplete_details")
    IncompleteDetails incompleteDetails;


    /**
     * The Unix timestamp (in seconds) for when the message was completed.
     */
    @JsonProperty("completed_at")
    private Integer completedAt;

    /**
     * The Unix timestamp (in seconds) for when the message was marked as incomplete.
     */
    @JsonProperty("incomplete_at")
    private Integer incompleteAt;


    /**
     * The entity that produced the message. One of user or assistant.
     */
    String role;

    /**
     * The content of the message in an array of text and/or images.
     */
    List<MessageContent> content;

    /**
     * If applicable, the ID of the assistant that authored this message.
     */
    @JsonProperty("assistant_id")
    String assistantId;

    /**
     *The ID of the run associated with the creation of this message. Value is null when messages are created manually using the create message or create thread endpoints.
     */
    @JsonProperty("run_id")
    String runId;

    /**
     * A list of files attached to the message, and the tools they were added to.
     */
    List<Attachment> attachments;

    /**
     * Set of 16 key-value pairs that can be attached to an object.
     * This can be useful for storing additional information about the object in a structured format.
     * Keys can be a maximum of 64 characters long, and values can be a maximum of 512 characters long.
     */
    Map<String, String> metadata;
}
