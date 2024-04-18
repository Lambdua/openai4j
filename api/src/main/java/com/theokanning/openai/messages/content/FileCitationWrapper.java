package com.theokanning.openai.messages.content;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author LiangTao
 * @date 2024年04月18 16:47
 **/
public class FileCitationWrapper {
    /**
     * Always file_citation.
     */
    String type;

    /**
     * The text in the message content that needs to be replaced
     */
    String text;

    @JsonProperty("file_citation")
    FileCitation fileCitation;


    @JsonProperty("start_index")
    Integer startIndex;

    @JsonProperty("end_index")
    Integer endIndex;


}
