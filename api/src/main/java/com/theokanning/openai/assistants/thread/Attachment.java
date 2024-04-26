package com.theokanning.openai.assistants.thread;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.theokanning.openai.assistants.assistant.Tool;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author LiangTao
 * @date 2024年04月18 15:24
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Attachment {

    /**
     * A list of File IDs that the message should use.
     * Defaults to an empty list.
     * There can be a maximum of 10 files attached to a message.
     * Useful for tools like retrieval and code_interpreter that can access and use files.
     */
    @JsonProperty("file_id")
    String fileId;

    /**
     * A list of tools that the files should be added
     */
    List<Tool> tools;

}
