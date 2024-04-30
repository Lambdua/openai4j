package com.theokanning.openai.assistants.vector_store;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author LiangTao
 * @date 2024年04月18 17:55
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VectorStore {

    /**
     * The identifier, which can be referenced in API endpoints.
     */
    String id;

    /**
     * The object type, which is always vector_store.
     */
    String object;

    /**
     * The Unix timestamp (in seconds) for when the vector store was created.
     */
    @JsonProperty("created_at")
    Integer createdAt;

    /**
     * The name of the vector store.
     */
    String name;

    /**
     * The byte size of the vector store.
     */
    Integer bytes;


    @JsonProperty("file_counts")
    FileCounts fileCounts;

    /**
     * The status of the vector store, which can be either expired, in_progress, or completed. A status of completed indicates that the vector store is ready for use.
     */
    String status;

    /**
     * The expiration policy for a vector store.
     */
    @JsonProperty("expires_after")
    ExpiresAfter expiresAfter;

    /**
     * The Unix timestamp (in seconds) for when the vector store will expire.
     */
    @JsonProperty("expires_at")
    Integer expiresAt;

    /**
     * The Unix timestamp (in seconds) for when the vector store was last active.
     */
    @JsonProperty("last_active_at")
    Integer lastActiveAt;

    /**
     * Set of 16 key-value pairs that can be attached to an object. This can be useful for storing additional information about the object in a structured format. Keys can be a maximum of 64 characters long and values can be a maxium of 512 characters long.
     */
    Map<String, String> metadata;

}
