package com.theokanning.openai.assistants.assistant;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LiangTao
 * @date 2024年09月19 10:53
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileSearch {
    /**
     * The maximum number of results the file search tool should output. The default is 20 for gpt-4* models and 5 for gpt-3.5-turbo. This number should be between 1 and 50 inclusive.
     */
    @JsonProperty("max_num_results")
    Integer maxNumResults;

    /**
     * The ranking options for the file search. If not specified, the file search tool will use the auto ranker and a score_threshold of 0.
     */
    @JsonProperty("ranking_options")
    FileSearchRankingOptions rankingOptions;

}
