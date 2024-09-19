package com.theokanning.openai.assistants.assistant;

/**
 * @author LiangTao
 * @date 2024年04月18 13:36
 **/

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileSearchTool implements Tool {
    final String type = "file_search";


    @JsonProperty("file_search")
    FileSearch fileSearch;


}
