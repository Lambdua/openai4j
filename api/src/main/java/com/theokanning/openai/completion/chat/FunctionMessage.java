package com.theokanning.openai.completion.chat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @deprecated see {@link ToolMessage}
 * @author LiangTao
 * @date 2024年04月10 10:37
 **/
@Data
@NoArgsConstructor
@Deprecated
public class FunctionMessage implements ChatMessage {
    private final String role = ChatMessageRole.FUNCTION.value();

    //The contents of the function message.
    private String content;

    //The name of the function to call.
    private String name;

    public FunctionMessage(String content) {
        this.content = content;
    }

    public FunctionMessage(String content, String name) {
        this.content = content;
        this.name = name;
    }

    @Override
    @JsonIgnore
    public String getTextContent() {
        return content;
    }
}
