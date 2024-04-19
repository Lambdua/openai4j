package com.theokanning.openai.assistants.vector_store_file_batch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.theokanning.openai.assistants.vector_store.FileCounts;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * https://platform.openai.com/docs/api-reference/vector-stores-file-batches/batch-object
 *
 * @author LiangTao
 * @date 2024年04月19 15:16
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VectorStoreFilesBatch {

    /**
     * The identifier, which can be referenced in API endpoints.
     */
    String id;

    /**
     * The object type, which is always vector_store.file_batch.
     */
    String object;

    /**
     * The Unix timestamp (in seconds) for when the vector store files batch was created.
     */
    @JsonProperty("created_at")
    Integer createdAt;

    /**
     * The ID of the vector store that the File is attached to.
     */
    @JsonProperty("vector_store_id")
    String vectorStoreId;

    /**
     * The status of the vector store files batch, which can be either in_progress, completed, cancelled or failed.
     */
    String status;

    /**
     * The number of files in the batch.
     */
    @JsonProperty("file_counts")
    FileCounts fileCounts;


}
