package com.theokanning.openai.assistants.message.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author LiangTao
 * @date 2024年04月23 15:15
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Delta {
    /**
     * The entity that produced the message. One of user or assistant.
     */
    String role;

    List<DeltaContent> content;
}
