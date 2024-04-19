package com.theokanning.openai.vector.store;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author LiangTao
 * @date 2024年04月19 15:02
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModifyVectorStoreRequest {
    String name;

    @JsonProperty("expires_after")
    ExpiresAfter expiresAfter;

    Map<String, String> metadata;
}
