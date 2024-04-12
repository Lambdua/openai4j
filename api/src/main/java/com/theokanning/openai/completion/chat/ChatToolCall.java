package com.theokanning.openai.completion.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatToolCall {

    /**
     * @see https://community.openai.com/t/gpt-4-turbo-model-function-call-doesnt-work/712218
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    int index;

    /**
     * The ID of the tool call
     */
    String id;

    /**
     * The type of the tool. Currently, only function is supported.
     */
    String type;


    /**
     * The function that the model called.
     */
    ChatFunctionCall function;

    public ChatToolCall(int index, String id, String type) {
        this.index = index;
        this.id = id;
        this.type = type;
        this.function = new ChatFunctionCall();
    }
}
