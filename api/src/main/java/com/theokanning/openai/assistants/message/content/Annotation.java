package com.theokanning.openai.assistants.message.content;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An annotation for a text Message
 * <p>
 * https://platform.openai.com/docs/api-reference/messages/object
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Annotation {

    Integer index;

    /**
     * file_citation/file_path
     */
    String type;

    /**
     * The text in the message content that needs to be replaced
     */
    String text;

    /**
     * File path details, only present when type == file_path
     */
    @JsonProperty("file_path")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    FilePath filePath;

    @JsonProperty("file_citation")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    FileCitation fileCitation;


    @JsonProperty("start_index")
    Integer startIndex;

    @JsonProperty("end_index")
    Integer endIndex;


}
