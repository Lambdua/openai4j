package com.theokanning.openai.completion.chat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.theokanning.openai.utils.JsonUtil;
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

    /**
     * when response_format is json_schema, the assistant can return a refusal message.
     */
    private String refusal;

    /**
     * Data about a previous audio response from the model.
     */
    private AssistantMessageAudio audio;


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

    /**
     * Deserializes the message to an object of the specified target class.
     *
     * @param targetClass the type of the object
     * @return the deserialized object
     **/
    public <T> T parsed(Class<T> targetClass) {
        try {
            return JsonUtil.getInstance().readValue(getTextContent(), targetClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
