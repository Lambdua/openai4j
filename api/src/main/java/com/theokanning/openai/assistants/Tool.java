package com.theokanning.openai.assistants;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author LiangTao
 * @date 2024年04月18 13:35
 **/
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FunctionTool.class, name = "function"),
        @JsonSubTypes.Type(value = CodeInterpreterTool.class, name = "code_interpreter"),
        @JsonSubTypes.Type(value = FileSearchTool.class, name = "file_search")
})
public interface Tool {
    String getType();
}
