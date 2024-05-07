package com.theokanning.openai.service;

import com.theokanning.openai.embedding.Embedding;
import com.theokanning.openai.embedding.EmbeddingRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;


public class EmbeddingTest {

    com.theokanning.openai.service.OpenAiService service = new OpenAiService();

    @Test
    void createEmbeddings() {
        EmbeddingRequest embeddingRequest = EmbeddingRequest.builder()
                .model("text-embedding-ada-002")
                .input("The food was delicious and the waiter...")
                .encodingFormat("base64")
                .build();

        List<Embedding> embeddings = service.createEmbeddings(embeddingRequest).getData();

        assertFalse(embeddings.isEmpty());
        assertFalse(embeddings.get(0).getEmbedding().isEmpty());
    }
}
