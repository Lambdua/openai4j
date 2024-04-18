package com.theokanning.openai.threads;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.theokanning.openai.assistants.ToolResources;
import com.theokanning.openai.messages.MessageRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Creates a thread
 * <p>
 * https://platform.openai.com/docs/api-reference/threads/createThread
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ThreadRequest {
    /**
     * A list of messages to start the thread with. Optional.
     */
    List<MessageRequest> messages;

    /**
     * A set of resources that are used by the assistant's tools.
     * The resources are specific to the type of tool.
     * For example, the code_interpreter tool requires a list of file IDs, while the file_search tool requires a list of vector store IDs.
     */
    @JsonProperty("tool_resources")
    ToolResources toolResources;

    /**
     * Set of 16 key-value pairs that can be attached to an object.
     * This can be useful for storing additional information about the object in a structured format.
     * Keys can be a maximum of 64 characters long, and values can be a maximum of 512 characters long.
     */
    Map<String, String> metadata;
}
