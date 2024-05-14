package com.theokanning.openai.fine_tuning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LiangTao
 * @date 2024年05月14 10:11
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Integrations {
    /**
     * The type of integration to enable. Currently, only "wandb" (Weights and Biases) is supported.
     */
    private String type;

    /**
     * The settings for your integration with Weights and Biases. This payload specifies the project that metrics will be sent to.<br>
     * Optionally, you can set an explicit display name for your run, add tags to your run, and set a default entity (team, username, etc) to be associated with your run.
     */
    private Wandb wandb;
}
