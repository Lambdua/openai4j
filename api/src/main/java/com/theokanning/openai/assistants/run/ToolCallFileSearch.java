package com.theokanning.openai.assistants.run;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.theokanning.openai.assistants.assistant.FileSearchRankingOptions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCallFileSearch {
    /**
     * The ranking options for the file search.
     */
    @JsonProperty("ranking_options")
    private FileSearchRankingOptions rankingOptions;

    /**
     * The results of the file search.
     */
    private List<ToolCallFileSearchResult> results;
}
