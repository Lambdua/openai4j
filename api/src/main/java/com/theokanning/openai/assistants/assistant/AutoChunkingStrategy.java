package com.theokanning.openai.assistants.assistant;

import lombok.Data;

/**
 * @author LiangTao
 * @date 2024年06月05 10:57
 **/
@Data
public class AutoChunkingStrategy implements ChunkingStrategy {
    public static final AutoChunkingStrategy instance = new AutoChunkingStrategy();

    private final String type = "auto";

}
