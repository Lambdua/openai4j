package com.theokanning.openai.assistants.vector_store;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LiangTao
 * @date 2024年04月18 17:57
 **/
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ExpiresAfter {
    /**
     * Anchor timestamp after which the expiration policy applies. Supported anchors: last_active_at.
     */
    String anchor;

    /**
     * The number of days after the anchor time that the vector store will expire.
     */
    Integer days;
}
