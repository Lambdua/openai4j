package com.theokanning.openai.completion.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LiangTao
 * @date 2024年05月07 22:46
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreamOption {

    public static final StreamOption INCLUDE = new StreamOption(true);

    public static final StreamOption EXCLUDE = new StreamOption(false);
    /**
     * If set, an additional chunk will be streamed before the data: [DONE] message.
     * The usage field on this chunk shows the token usage statistics for the entire request, and the choices field will always be an empty array.
     * All other chunks will also include a usage field, but with a null value.
     */
    @JsonProperty("include_usage")
    Boolean includeUsage;

}
