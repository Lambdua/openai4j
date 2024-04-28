package com.theokanning.openai.completion.chat;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class ChatTool {

    @NonNull
    private Object function;

    /**
     * The name of the tool being called, only function supported for now.
     */
    @NonNull
    private String type = "function";


    public ChatTool(@NonNull Object function) {
        this.function = function;
    }

}
