package com.theokanning.openai.assistants.assistant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LiangTao
 * @date 2024年04月18 13:48
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ToolResources {
    @JsonProperty("code_interpreter")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    CodeInterpreterResources codeInterpreter;

    @JsonProperty("file_search")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    FileSearchResources fileSearch;

    public ToolResources(CodeInterpreterResources codeInterpreter) {
        this.codeInterpreter = codeInterpreter;
    }

    public ToolResources(FileSearchResources fileSearch) {
        this.fileSearch = fileSearch;
    }
}
