package com.theokanning.openai.assistants.assistant;

import lombok.Data;

/**
 * @author LiangTao
 * @date 2024年04月18 13:35
 **/
@Data
public class CodeInterpreterTool implements Tool {
    final String type = "code_interpreter";
}
