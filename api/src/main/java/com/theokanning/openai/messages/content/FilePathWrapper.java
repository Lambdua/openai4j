package com.theokanning.openai.messages.content;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author LiangTao
 * @date 2024年04月18 16:59
 **/
public class FilePathWrapper {
    /**
     * Always file_path.
     */
    String type;

    /**
     * The text in the message content that needs to be replaced
     */
    String text;

    @JsonProperty("file_path")
    FilePath filePath;


    @JsonProperty("start_index")
    Integer startIndex;

    @JsonProperty("end_index")
    Integer endIndex;


}
