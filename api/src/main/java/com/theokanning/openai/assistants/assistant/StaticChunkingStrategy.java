package com.theokanning.openai.assistants.assistant;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author LiangTao
 * @date 2024年06月05 10:59
 **/
@Data
public class StaticChunkingStrategy implements ChunkingStrategy {
    private final String type = "static";

    @JsonProperty("static")
    private Static aStatic;

    public static StaticChunkingStrategy of(Integer maxChunkSizeTokens, Integer chunkOverlapTokens) {
        StaticChunkingStrategy staticChunkingStrategy = new StaticChunkingStrategy();
        Static aStatic = new Static();
        aStatic.setMaxChunkSizeTokens(maxChunkSizeTokens);
        aStatic.setChunkOverlapTokens(chunkOverlapTokens);
        staticChunkingStrategy.setAStatic(aStatic);
        return staticChunkingStrategy;
    }


    @Data
    public static class Static {
        /**
         * The maximum number of tokens in each chunk. The default value is 800. The minimum value is 100 and the maximum value is 4096.
         */
        @JsonProperty("max_chunk_size_tokens")
        private Integer maxChunkSizeTokens;

        /**
         * The number of tokens that overlap between chunks. The default value is 400.
         * <p>
         * Note that the overlap must not exceed half of max_chunk_size_tokens.
         */
        @JsonProperty("chunk_overlap_tokens")
        private Integer chunkOverlapTokens;

    }
}
