package com.theokanning.openai.service;

import com.theokanning.openai.completion.chat.AssistantMessage;
import com.theokanning.openai.completion.chat.ChatFunctionCall;

/**
 * Class that accumulates chat messages and provides utility methods for
 * handling message chunks and function calls within a chat stream. This
 * class is immutable.
 *
 * @author [Your Name]
 */
public class ChatMessageAccumulator {

    private final AssistantMessage messageChunk;
    private final AssistantMessage accumulatedMessage;

    /**
     * Constructor that initializes the message chunk and accumulated message.
     *
     * @param messageChunk       The message chunk.
     * @param accumulatedMessage The accumulated message.
     */
    public ChatMessageAccumulator(AssistantMessage messageChunk, AssistantMessage accumulatedMessage) {
        this.messageChunk = messageChunk;
        this.accumulatedMessage = accumulatedMessage;
    }

    /**
     * Checks if the accumulated message contains a function call.
     *
     * @return true if the accumulated message contains a function call, false otherwise.
     */
    public boolean isFunctionCall() {
        AssistantMessage asstMsg = getAccumulatedMessage();
        return (asstMsg.getFunctionCall() != null && asstMsg.getFunctionCall().getName() != null) ||
                (asstMsg.getToolCalls() != null && !asstMsg.getToolCalls().isEmpty());
    }

    /**
     * Checks if the accumulated message contains a chat message.
     *
     * @return true if the accumulated message contains a chat message, false otherwise.
     */
    public boolean isChatMessage() {
        return !isFunctionCall();
    }

    /**
     * Retrieves the message chunk.
     *
     * @return the message chunk.
     */
    public AssistantMessage getMessageChunk() {
        return messageChunk;
    }

    /**
     * Retrieves the accumulated message.
     *
     * @return the accumulated message.
     */
    public AssistantMessage getAccumulatedMessage() {
        return accumulatedMessage;
    }

    /**
     * Retrieves the function call from the message chunk.
     * This is equivalent to getMessageChunk().getFunctionCall().
     *
     * @return the function call from the message chunk.
     */
    public ChatFunctionCall getChatFunctionCallChunk() {
        AssistantMessage msC = getMessageChunk();
        ChatFunctionCall functionCall = msC.getFunctionCall();
        if (functionCall == null) {
            functionCall = msC.getToolCalls().get(0).getFunction();
        }
        return functionCall;
    }

    /**
     * Retrieves the function call from the accumulated message.
     * This is equivalent to getAccumulatedMessage().getFunctionCall().
     *
     * @return the function call from the accumulated message.
     */
    public ChatFunctionCall getAccumulatedChatFunctionCall() {
        return getAccumulatedMessage().getFunctionCall();
    }
}
