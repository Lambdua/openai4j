package com.theokanning.openai.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.theokanning.openai.messages.content.ImageFileWrapper;
import com.theokanning.openai.messages.content.TextWrapper;
import lombok.Data;


/**
 * Represents the content of a message
 * <p>
 * https://platform.openai.com/docs/api-reference/messages/object
 */
@Data
public class MessageContent {

    /**
     * The text content that is part of a message.
     */
    TextWrapper text;

    /**
     * References an image File in the content of a message.
     */
    @JsonProperty("image_file")
    ImageFileWrapper imageFile;
}
