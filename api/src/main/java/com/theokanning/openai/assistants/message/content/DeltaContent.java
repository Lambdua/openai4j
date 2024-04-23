package com.theokanning.openai.assistants.message.content;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LiangTao
 * @date 2024年04月23 15:16
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeltaContent {

    Integer index;

    /**
     * text/image_file
     */
    String type;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    Text text;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("image_file")
    ImageFile imageFile;

}
