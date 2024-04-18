package com.theokanning.openai.assistants;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author LiangTao
 * @date 2024年04月18 14:00
 **/
@Data
public class VectorStore {

    @JsonProperty("file_ids")
    private List<String> fileIds;

    /**
     * Set of 16 key-value pairs that can be attached to a vector store. This can be useful for storing additional information about the vector store in a structured format. Keys can be a maximum of 64 characters long and values can be a maxium of 512 characters long.
     */
    @JsonProperty("metadata")
    private Map<String, String> metadata;


}
