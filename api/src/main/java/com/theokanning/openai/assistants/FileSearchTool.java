package com.theokanning.openai.assistants;

/**
 * @author LiangTao
 * @date 2024年04月18 13:36
 **/

import lombok.Data;

@Data
public class FileSearchTool implements Tool {
    final String type = "file_search";
}
