package com.theokanning.openai.messages.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LiangTao
 * @date 2024年04月18 16:42
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageFileWrapper {
    /**
     * always image_file
     */
    String type;

    @JsonProperty("image_file")
    ImageFile imageFile;
}
