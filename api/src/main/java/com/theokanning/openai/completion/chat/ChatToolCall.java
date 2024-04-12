package com.theokanning.openai.completion.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatToolCall {

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
