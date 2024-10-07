package com.theokanning.openai.assistants.run;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCallFileSearchResult {
    /**
     * The ID of the file that result was found in.
     */
    @JsonProperty("file_id")
    private String fileId;

    /**
     * The name of the file that result was found in.
     */
    @JsonProperty("file_name")
    private String fileName;

    /**
     * The score of the result. All values must be a floating point number between 0 and 1.
     */
    private Double score;

    /**
     * The content of the result that was found. The content is only included if requested via the include query parameter.
     */
    private List<ToolCallFileSearchResultContent> content;
}
