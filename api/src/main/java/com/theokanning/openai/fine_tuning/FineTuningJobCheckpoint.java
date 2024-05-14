package com.theokanning.openai.fine_tuning;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author LiangTao
 * @date 2024年05月14 10:23
 **/
@Data
public class FineTuningJobCheckpoint {
    /**
     * id
     * string
     *
     * The checkpoint identifier, which can be referenced in the API endpoints.
     *
     * created_at
     * integer
     *
     * The Unix timestamp (in seconds) for when the checkpoint was created.
     *
     * fine_tuned_model_checkpoint
     * string
     *
     * The name of the fine-tuned checkpoint model that is created.
     *
     * step_number
     * integer
     *
     * The step number that the checkpoint was created at.
     *
     * metrics
     * object
     *
     * Metrics at the step number during the fine-tuning job.
     *
     *
     * Show properties
     * fine_tuning_job_id
     * string
     *
     * The name of the fine-tuning job that this checkpoint was created from.
     *
     * object
     * string
     *
     * The object type, which is always "fine_tuning.job.checkpoint".
     */

    /**
     * The checkpoint identifier, which can be referenced in the API endpoints.
     */
    String id;

    /**
     * The Unix timestamp (in seconds) for when the checkpoint was created.
     */
    @JsonProperty("created_at")
    Long createdAt;

    /**
     * The name of the fine-tuned checkpoint model that is created.
     */
    @JsonProperty("fine_tuned_model_checkpoint")
    String fineTunedModelCheckpoint;

    /**
     * The step number that the checkpoint was created at.
     */
    @JsonProperty("step_number")
    Integer stepNumber;

    /**
     * Metrics at the step number during the fine-tuning job.
     */
    Metrics metrics;

    /**
     * The name of the fine-tuning job that this checkpoint was created from.
     */
    @JsonProperty("fine_tuning_job_id")
    String fineTuningJobId;

    /**
     * The object type, which is always "fine_tuning.job.checkpoint".
     */
    String object;

}
