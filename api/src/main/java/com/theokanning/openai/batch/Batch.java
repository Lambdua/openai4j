package com.theokanning.openai.batch;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author LiangTao
 * @date 2024年04月23 13:45
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Batch {
    /**
     * id
     */
    String id;

    /**
     * The object type, which is always batch.
     */
    String object;

    /**
     * The OpenAI API endpoint used by the batch.
     */
    String endpoint;

    Errors errors;

    /**
     * The ID of the input file for the batch.
     */
    @JsonProperty("input_file_id")
    String inputFileId;

    /**
     * The time frame within which the batch should be processed.
     */
    @JsonProperty("completion_window")
    String completionWindow;

    /**
     * The current status of the batch.
     */
    String status;

    /**
     * The ID of the file containing the outputs of successfully executed requests.
     */
    @JsonProperty("output_file_id")
    String outputFileId;

    /**
     * The ID of the file containing the outputs of requests with errors.
     */
    @JsonProperty("error_file_id")
    String errorFileId;

    /**
     * The Unix timestamp (in seconds) for when the batch was created.
     */
    @JsonProperty("created_at")
    Integer createdAt;

    /**
     * The Unix timestamp (in seconds) for when the batch started processing.
     */
    @JsonProperty("in_progress_at")
    Integer inProgressAt;

    /**
     * The Unix timestamp (in seconds) for when the batch will expire.
     */
    @JsonProperty("expires_at")
    Integer expiresAt;

    /**
     * The Unix timestamp (in seconds) for when the batch started finalizing.
     */
    @JsonProperty("finalizing_at")
    Integer finalizingAt;

    /**
     * The Unix timestamp (in seconds) for when the batch was completed.
     */
    @JsonProperty("completed_at")
    Integer completedAt;

    /**
     * The Unix timestamp (in seconds) for when the batch failed.
     */
    @JsonProperty("failed_at")
    Integer failedAt;

    /**
     * The Unix timestamp (in seconds) for when the batch expired.
     */
    @JsonProperty("expired_at")
    Integer expiredAt;

    /**
     * The Unix timestamp (in seconds) for when the batch started cancelling.
     */
    @JsonProperty("cancelling_at")
    Integer cancellingAt;

    /**
     * The Unix timestamp (in seconds) for when the batch was cancelled.
     */
    @JsonProperty("cancelled_at")
    Integer cancelledAt;

    /**
     * The request counts for different statuses within the batch.
     */
    @JsonProperty("request_counts")
    RequestCounts requestCounts;

    /**
     * Set of 16 key-value pairs that can be attached to an object. This can be useful for storing additional information about the object in a structured format. Keys can be a maximum of 64 characters long and values can be a maxium of 512 characters long.
     */

    Map<String, String> metadata;
}
