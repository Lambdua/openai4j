package com.theokanning.openai.assistants.assistant;

/**
 * @author LiangTao
 * @date 2024年04月18 13:36
 **/

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileSearchTool implements Tool {
    final String type = "file_search";

    /**
     * The maximum number of results the file search tool should output. The default is 20 for gpt-4* models and 5 for gpt-3.5-turbo. This number should be between 1 and 50 inclusive.
     */
    @JsonProperty("max_num_results")
    Integer maxNumResults;
}
