package com.theokanning.openai.assistants.run;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author LiangTao
 * @date 2024年04月28 11:58
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifyRunRequest {
    Map<String, String> metadata;

}
