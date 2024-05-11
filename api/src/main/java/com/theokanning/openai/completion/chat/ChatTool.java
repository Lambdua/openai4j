package com.theokanning.openai.completion.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatTool {
    public ChatTool(@NonNull Object function) {
        this.function = function;
    }

    /**
     * The name of the tool being called, only function supported for now.
     */
    @NonNull
    private String type = "function";


    /**
     * recommend use {@link com.theokanning.openai.function.FunctionDefinition} .
     * also you can customer your own function
     */
    @NonNull
    private Object function;




}
