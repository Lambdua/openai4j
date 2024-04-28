package com.theokanning.openai.assistants.assistant;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class VectorStoreFileRequest {

    @JsonProperty("file_id")
    String fileId;
}
