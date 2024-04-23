package com.theokanning.openai.assistants.run_step;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LiangTao
 * @date 2024年04月23 15:24
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RunStepDelta {
    /**
     * The identifier of the message, which can be referenced in API endpoints.
     */
    String id;

    /**
     * The object type, which is always thread.run.step.delta.
     */
    String object;

    /**
     * The delta containing the fields that have changed on the run step.
     */
    Delta delta;

}
