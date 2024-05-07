package com.theokanning.openai.embedding;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * Creates an embedding vector representing the input text.
 * <p>
 * https://beta.openai.com/docs/api-reference/embeddings/create
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class EmbeddingRequest {
    /**
     * The name of the model to use.
     * Required if using the new v1/embeddings endpoint.
     */
    @NonNull
    String model;
    /**
     * input text to embed, encoded as a string or array of tokens. To embed multiple inputs in a single request, pass an array of strings or array of token arrays.
     * The input must not exceed the max input tokens for the model (8192 tokens for text-embedding-ada-002), cannot be an empty string, and any array must be 2048 dimensions or less. <br> <br>
     * <p>
     * input allow type of :String/Integer/List<String>/List<Integer>/List<List<Integer>>   <br>
     * Also you can use {@link InternalBuilder#input(Object)} to check the input, it will automatically check the input type.
     */
    @NonNull
    Object input;

    public static EmbeddingRequestBuilder builder() {
        return new InternalBuilder();
    }

    private static class InternalBuilder extends EmbeddingRequestBuilder {

        @Builder
        @SuppressWarnings("unchecked")
        public EmbeddingRequestBuilder input(@NonNull Object input) {
            if (input instanceof String) {
                return super.input(input);
            }
            if (input instanceof List) {
                List tem = (List) input;
                if (tem.stream().allMatch(String.class::isInstance)) {
                    return super.input(input);
                }
                if (tem.stream().allMatch(Integer.class::isInstance)) {
                    return super.input(input);
                }

                if (tem.stream().allMatch(List.class::isInstance)) {
                    List<List> tem2 = (List<List>) input;
                    if (tem2.stream().flatMap(List::stream).allMatch(Integer.class::isInstance)) {
                        return super.input(input);
                    }
                }
            }
            throw new IllegalArgumentException("input must be String/Integer/List<String>/List<Integer>/List<List<Integer>>");
        }
    }

    /**
     * The format to return the embeddings in. Can be either float or base64.
     */
    @JsonProperty("encoding_format")
    String encodingFormat;

    /**
     * The number of dimensions the resulting output embeddings should have. Only supported in text-embedding-3 and later models.
     */
    Integer dimensions;

    /**
     * A unique identifier representing your end-user, which will help OpenAI to monitor and detect abuse.
     */
    String user;


}
