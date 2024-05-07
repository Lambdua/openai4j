package com.theokanning.openai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.theokanning.openai.embedding.Embedding;
import com.theokanning.openai.embedding.EmbeddingRequest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class EmbeddingTest {

    OpenAiService service = new OpenAiService();

    @Test
    void createEmbeddings() {
        EmbeddingRequest embeddingRequest = EmbeddingRequest.builder()
                .model("text-embedding-ada-002")
                .input("The food was delicious and the waiter...")
                .encodingFormat("base64")
                .build();

        List<Embedding> embeddings = service.createEmbeddings(embeddingRequest).getData();

        assertFalse(embeddings.isEmpty());
        Object embedding = embeddings.get(0).getEmbedding();
        assertInstanceOf(String.class, embedding);
    }

    @Test
    void createEmbeddings2() throws JsonProcessingException {
        EmbeddingRequest embeddingRequest = EmbeddingRequest.builder()
                .model("text-embedding-ada-002")
                .input(Arrays.asList(Arrays.asList(1, 23, 5), Arrays.asList(1, 23, 5), Arrays.asList(1, 2390, 5123)))
                .build();
        List<Embedding> embeddings = service.createEmbeddings(embeddingRequest).getData();
        assertFalse(embeddings.isEmpty());
        assertEquals(3, embeddings.size());
        Object embedding = embeddings.get(0).getEmbedding();
        assertInstanceOf(List.class, embedding);
        List<Double> list = (List<Double>) embedding;
        assertFalse(list.isEmpty());
    }

}
