package com.theokanning.openai.messages.content;

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

    /**
     * A citation within the message that points to a specific quote from a specific File associated with the assistant or the message.
     * Generated when the assistant uses the "file_search" tool to search files.
     **/
    @JsonProperty("file_citation")
    FileCitationWrapper fileCitation;

    /**
     * File path details, only present when type == file_path
     */
    @JsonProperty("file_path")
    FilePathWrapper filePath;

}
