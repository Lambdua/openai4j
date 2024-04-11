package com.theokanning.openai.completion.chat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LiangTao
 * @date 2024年04月10 10:35
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolMessage implements ChatMessage {

    private final String role = ChatMessageRole.TOOL.value();

    private String content;

    //Tool call that this message is responding to.
    @JsonProperty("tool_call_id")
    private String toolCallId;


    @Override
    @JsonIgnore
    public String getTextContent() {
        return content;
    }

    //官方文档中没有提到的字段
    @Override
    public String getName() {
        return null;
    }
}
