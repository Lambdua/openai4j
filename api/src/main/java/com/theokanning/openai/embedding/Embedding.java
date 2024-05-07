package com.theokanning.openai.embedding;

import lombok.Data;

/**
 * Represents an embedding returned by the embedding api
 * <p>
 * https://beta.openai.com/docs/api-reference/classifications/create
 */
@Data
public class Embedding {

    /**
     * The type of object returned, should be "embedding"
     */
    String object;

    /**
     * The embedding vector array or string
     */
    Object embedding;

    /**
     * The position of this embedding in the list
     */
    Integer index;
}
