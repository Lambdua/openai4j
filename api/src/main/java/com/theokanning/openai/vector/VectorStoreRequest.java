package com.theokanning.openai.vector;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author LiangTao
 * @date 2024年04月18 18:01
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VectorStoreRequest {

    /**
     * A list of File IDs that the vector store should use. Useful for tools like file_search that can access files.
     */
    @JsonProperty("file_ids")
    List<String> fileIds;

    /**
     * The name of the vector store.
     */
    String name;

    /**
     * The expiration policy for a vector store.
     */
    @JsonProperty("expires_after")
    ExpiresAfter expiresAfter;

    /**
     * Set of 16 key-value pairs that can be attached to an object. This can be useful for storing additional information about the object in a structured format. Keys can be a maximum of 64 characters long and values can be a maxium of 512 characters long.
     */
    Map<String, String> metadata;


}
