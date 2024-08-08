package com.theokanning.openai.completion.chat;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * chat 接口的请求message 接口
 * @see <a href="https://platform.openai.com/docs/api-reference/chat/completion/create">Chat Completion</a>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "role", defaultImpl = AssistantMessage.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AssistantMessage.class, name = "assistant"),
        @JsonSubTypes.Type(value = ToolMessage.class, name = "tool"),
        @JsonSubTypes.Type(value = UserMessage.class, name = "user"),
        @JsonSubTypes.Type(value = SystemMessage.class, name = "system"),
        @JsonSubTypes.Type(value = FunctionMessage.class, name = "function")
})
public interface ChatMessage {
    String getRole();

    String getTextContent();

    String getName();

}
