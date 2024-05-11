package com.theokanning.openai.completion.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.theokanning.openai.assistants.message.content.ImageFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * @author LiangTao
 * @date 2024年04月10 10:26
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageContent {

    /**
     * The type of the content. Either "text" or "image_url".
     */
    @NonNull
    private String type;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String text;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("image_url")
    private ImageUrl imageUrl;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("image_file")
    private ImageFile imageFile;


    public ImageContent(String text) {
        this.type = "text";
        this.text = text;
    }

    public ImageContent(ImageUrl imageUrl) {
        this.type = "image_url";
        this.imageUrl = imageUrl;
    }

}
