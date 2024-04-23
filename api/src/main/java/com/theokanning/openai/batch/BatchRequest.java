package com.theokanning.openai.batch;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;

/**
 * batch create quest
 *
 * @author LiangTao
 * @date 2024年04月23 13:42
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchRequest {
    /**
     * The ID of an uploaded file that contains requests for the new batch.
     * See upload file for how to upload a file.
     * https://platform.openai.com/docs/api-reference/batch/create
     * Your input file must be formatted as a JSONL file, and must be uploaded with the purpose `batch`.
     */
    @JsonProperty("input_file_id")
    @NonNull
    private String inputFileId;

    /**
     * The endpoint to be used for all requests in the batch. Currently only `/v1/chat/completions` is supported.
     */
    @NonNull
    private String endpoint;

    /**
     * The time frame within which the batch should be processed. Currently only `24h` is supported.
     */
    @JsonProperty("completion_window")
    @NonNull
    private String completionWindow;

    /**
     * Optional custom metadata for the batch.
     */
    private Map<String, String> metadata;


}
