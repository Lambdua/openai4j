package com.theokanning.openai.assistants.assistant;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author LiangTao
 * @date 2024年06月05 10:56
 **/
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AutoChunkingStrategy.class, name = "auto"),
        @JsonSubTypes.Type(value = StaticChunkingStrategy.class, name = "static"),
})
public interface ChunkingStrategy {
    String getType();

}
