package com.theokanning.openai.assistants.message.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * References an image File int eh content of a message.
 * <p>
 * /https://platform.openai.com/docs/api-reference/messages/object
 */
@Data
@NoArgsConstructor
public class ImageFile {

    /**
     * The File ID of the image in the message content.
     */
    @JsonProperty("file_id")
    String fileId;

    String detail;

    public ImageFile(String fileId) {
        this(fileId,"low");
    }

    public ImageFile(String fileId, String detail) {
        this.fileId = fileId;
        this.detail = detail;
    }
}
