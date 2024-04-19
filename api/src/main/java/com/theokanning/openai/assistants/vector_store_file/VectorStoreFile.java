package com.theokanning.openai.assistants.vector_store_file;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.theokanning.openai.common.LastError;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * https://platform.openai.com/docs/api-reference/vector-stores-files/file-object
 *
 * @author LiangTao
 * @date 2024年04月19 15:07
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VectorStoreFile {

    /**
     * The identifier, which can be referenced in API endpoints.
     */
    String id;

    /**
     * The object type, which is always vector_store.file.
     */
    String object;

    /**
     * The Unix timestamp (in seconds) for when the vector store file was created.
     */
    @JsonProperty("created_at")
    Integer createdAt;

    /**
     * The ID of the vector store that the File is attached to.
     */
    @JsonProperty("vector_store_id")
    String vectorStoreId;

    /**
     * The status of the vector store file, which can be either in_progress, completed, cancelled, or failed. The status completed indicates that the vector store file is ready for use.
     */
    String status;

    /**
     * The last error associated with this vector store file. Will be null if there are no errors.
     */
    @JsonProperty("last_error")
    LastError lastError;
}
