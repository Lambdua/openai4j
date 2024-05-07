package example;

import com.theokanning.openai.embedding.Embedding;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.service.OpenAiService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * @author LiangTao
 * @date 2024年05月07 22:25
 **/
public class EmbeddingExample {
    public static void main(String[] args) {
        OpenAiService service = new OpenAiService();
        EmbeddingRequest embeddingRequest = EmbeddingRequest.builder()
                .model("text-embedding-ada-002")
                //You can use the following methods to set input for embedding
                .input("The food was delicious and the waiter...")
                // .input(Arrays.asList("test1", "test2", "test3"))
                // .input(Arrays.asList(Arrays.asList(1, 23, 5), Arrays.asList(1, 23, 5), Arrays.asList(1, 2390, 5123)))

                //if you want to use base64 encoding, you can use the following method and response embedding type will String , otherwise it will be List<Double>
                // .encodingFormat("base64")
                .build();
        List<Embedding> embeddings = service.createEmbeddings(embeddingRequest).getData();
        Object embedding = embeddings.get(0).getEmbedding();
        assertInstanceOf(List.class, embedding);
        List<Double> list = (List<Double>) embedding;
    }
}
