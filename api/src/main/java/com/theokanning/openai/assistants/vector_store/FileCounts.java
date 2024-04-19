package com.theokanning.openai.assistants.vector_store;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LiangTao
 * @date 2024年04月18 17:56
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileCounts {

    /**
     * The number of files that have been successfully processed.
     */
    @JsonProperty("in_progress")
    private Integer inProgress;

    /**
     * The number of files that have been successfully processed.
     */
    private Integer completed;

    /**
     * The number of files that have failed to process.
     */
    private Integer failed;

    /**
     * The number of files that were cancelled.
     */
    private Integer cancelled;

    /**
     * The total number of files.
     */
    private Integer total;
}
