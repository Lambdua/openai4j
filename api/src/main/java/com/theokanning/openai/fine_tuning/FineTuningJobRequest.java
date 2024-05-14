package com.theokanning.openai.fine_tuning;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;


/**
 * Request to create a fine tuning job
 * https://platform.openai.com/docs/api-reference/fine-tuning/create
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FineTuningJobRequest {
    /**
     * The name of the model to fine-tune.
     */
    @NonNull
    String model;

    /**
     * The ID of an uploaded file that contains validation data.
     * Optional.
     */
    @JsonProperty("validation_file")
    String validationFile;

    /**
     * The ID of an uploaded file that contains training data.
     */
    @NonNull
    @JsonProperty("training_file")
    String trainingFile;

    /**
     * The hyperparameters used for the fine-tuning job.
     */
    Hyperparameters hyperparameters;

    /**
     * A string of up to 40 characters that will be added to your fine-tuned model name.
     */
    String suffix;


    List<Integrations> integrations;

    /**
     * The seed controls the reproducibility of the job. Passing in the same seed and job parameters should produce the same results, but may differ in rare cases. If a seed is not specified, one will be generated for you.
     */
    Integer seed;
}
