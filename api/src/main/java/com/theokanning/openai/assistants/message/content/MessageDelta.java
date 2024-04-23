package com.theokanning.openai.assistants.message.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * stream message content
 * Represents a message delta i.e. any changed fields on a message during streaming.
 *
 * @author LiangTao
 * @date 2024年04月23 15:05
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDelta {
    /**
     * The identifier of the message, which can be referenced in API endpoints.
     */
    String id;

    /**
     * The object type, which is always thread.message.delta.
     */
    String object;

    /**
     * The delta containing the fields that have changed on the Message.
     */
    Delta delta;


}
