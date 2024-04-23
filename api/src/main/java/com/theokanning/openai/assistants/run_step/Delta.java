package com.theokanning.openai.assistants.run_step;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LiangTao
 * @date 2024年04月23 15:25
 **/
@NoArgsConstructor
@Data
@AllArgsConstructor
public class Delta {
    /**
     * The details of the run step.
     */
    @JsonProperty("step_details")
    StepDetails stepDetails;
}
