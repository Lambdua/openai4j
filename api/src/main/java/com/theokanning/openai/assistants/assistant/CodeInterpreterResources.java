package com.theokanning.openai.assistants.assistant;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * @author LiangTao
 * @date 2024年04月18 13:50
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodeInterpreterResources {

    /**
     * A list of file IDs made available to the code_interpreter tool. There can be a maximum of 20 files associated with the tool.
     */
    @JsonProperty("file_ids")
    private List<String> fileIds = Collections.emptyList();

    public static CodeInterpreterResources of(List<String> fileIds) {
        return new CodeInterpreterResources(fileIds);
    }
}
