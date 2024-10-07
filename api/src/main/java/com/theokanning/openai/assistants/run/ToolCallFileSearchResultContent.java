package com.theokanning.openai.assistants.run;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCallFileSearchResultContent {
    /**
     * The type of the content.
     */
    private String type;

    /**
     * The text content of the file.
     */
    private String text;
}
