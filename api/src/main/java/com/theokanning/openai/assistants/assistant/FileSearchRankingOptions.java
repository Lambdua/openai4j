package com.theokanning.openai.assistants.assistant;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author LiangTao
 * @date 2024年09月19 10:36
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileSearchRankingOptions {
    /**
     * The ranker to use for the file search. If not specified will use the auto ranker.
     */
    String ranker;

    /**
     * The score threshold for the file search. All values must be a floating point number between 0 and 1.
     */
    @NotNull
    @JsonProperty("score_threshold")
    Double scoreThreshold;

}
