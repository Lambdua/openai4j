package com.theokanning.openai.completion.chat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author LiangTao
 * @date 2024年04月10 10:31
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssistantMessage implements ChatMessage {
    final String role = ChatMessageRole.ASSISTANT.value();

    // The contents of the assistant message. Required unless tool_calls or function_call is specified.
    String content;

    //An optional name for the participant. Provides the model information to differentiate between participants of the same role.
    String name;

    @JsonProperty("tool_calls")
    List<ChatToolCall> toolCalls;

    /**
     * @deprecated Replaced by tool_calls
     */
    @Deprecated
    @JsonProperty("function_call")
    ChatFunctionCall functionCall;


    public AssistantMessage(String content) {
        this.content = content;
    }

    public AssistantMessage(String content, String name) {
        this.content = content;
        this.name = name;
    }

    @Override
    @JsonIgnore
    public String getTextContent() {
        return content;
    }
}
