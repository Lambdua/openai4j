package com.theokanning.openai.threads;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.theokanning.openai.assistants.ToolResources;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Represents a Thread with an assistant
 * <p>
 * https://platform.openai.com/docs/api-reference/threads/object
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Thread {
    /**
     * The identifier, which can be referenced in API endpoints.
     */
    String id;

    /**
     * The object type, which is always thread.
     */
    String object;

    /**
     * The Unix timestamp (in seconds) for when the thread was created.
     */
    @JsonProperty("created_at")
    int createdAt;

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
