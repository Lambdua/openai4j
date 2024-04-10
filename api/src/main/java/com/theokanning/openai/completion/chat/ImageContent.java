package com.theokanning.openai.completion.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @NonNull
    private String type;

    @JsonProperty("image_url")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String text;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("image_url")
    private ImageUrl imageUrl;

    public ImageContent(String text) {
        this.type = "text";
        this.text = text;
    }

    public ImageContent(ImageUrl imageUrl) {
        this.type = "image_url";
        this.imageUrl = imageUrl;
    }
}
