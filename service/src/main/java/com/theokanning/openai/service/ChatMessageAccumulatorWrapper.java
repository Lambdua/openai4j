package com.theokanning.openai.service;

import com.theokanning.openai.completion.chat.ChatCompletionChunk;

/**
 * Wrapper class of ChatMessageAccumulator
 *
 * @author Allen Hu
 * @date 2024/10/18
 */
public class ChatMessageAccumulatorWrapper {

    private final ChatMessageAccumulator chatMessageAccumulator;
    private final ChatCompletionChunk chatCompletionChunk;

    public ChatMessageAccumulatorWrapper(ChatMessageAccumulator chatMessageAccumulator, ChatCompletionChunk chatCompletionChunk) {
        this.chatMessageAccumulator = chatMessageAccumulator;
        this.chatCompletionChunk = chatCompletionChunk;
    }

    public ChatMessageAccumulator getChatMessageAccumulator() {
        return chatMessageAccumulator;
    }

    public ChatCompletionChunk getChatCompletionChunk() {
        return chatCompletionChunk;
    }
}
