package com.theokanning.openai.fine_tuning;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author LiangTao
 * @date 2024年05月14 10:24
 **/
@Data
public class Metrics {
    Integer step;

    @JsonProperty("train_loss")
    Double trainLoss;

    @JsonProperty("train_mean_token_accuracy")
    Double trainMeanTokenAccuracy;

    @JsonProperty("valid_loss")
    Double validLoss;

    @JsonProperty("valid_mean_token_accuracy")
    Double validMeanTokenAccuracy;

    @JsonProperty("full_valid_loss")
    Double fullValidLoss;

    @JsonProperty("full_valid_mean_token_accuracy")
    Double fullValidMeanTokenAccuracy;
}
