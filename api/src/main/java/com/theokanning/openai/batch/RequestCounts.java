package com.theokanning.openai.batch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LiangTao
 * @date 2024年04月23 13:48
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestCounts {
    /**
     * Total number of requests in the batch.
     */
    Integer total;

    /**
     * Number of requests that have been completed successfully.
     */
    Integer completed;

    /**
     * Number of requests that have failed.
     */
    Integer failed;
}
