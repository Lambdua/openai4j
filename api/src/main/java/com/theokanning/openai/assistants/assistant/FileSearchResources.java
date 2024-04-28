package com.theokanning.openai.assistants.assistant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.theokanning.openai.assistants.vector_store.VectorStore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author LiangTao
 * @date 2024年04月18 13:54
 **/
@Data
@NoArgsConstructor
public class FileSearchResources {

    /**
     * The vector store attached to this assistant. There can be a maximum of 1 vector store attached to the assistant.
     */
    @JsonProperty("vector_store_ids")
    List<String> vectorStoreIds;

    /**
     * A helper to create a vector store with file_ids and attach it to this assistant. There can be a maximum of 1 vector store attached to the assistant.
     */
    @JsonProperty("vector_stores")
    List<VectorStore> vectorStores;
}
