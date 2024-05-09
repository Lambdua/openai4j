package com.theokanning.openai.assistants.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.theokanning.openai.ListSearchParameters;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LiangTao
 * @date 2024年05月09 16:05
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageListSearchParameters {
    /**
     * A limit on the number of objects to be returned.
     * Limit can range between 1 and 100, and the default is 20
     */

    Integer limit;

    /**
     * Sort order by the 'created_at' timestamp of the objects.
     * 'asc' for ascending order and 'desc' for descending order.
     */
    ListSearchParameters.Order order;

    /**
     * A cursor for use in pagination. after is an object ID that defines your place in the list.
     * For instance, if you make a list request and receive 100 objects, ending with obj_foo,
     * your subsequent call can include after=obj_foo in order to fetch the next page of the list
     */
    String after;

    /**
     * A cursor for use in pagination. before is an object ID that defines your place in the list.
     * For instance, if you make a list request and receive 100 objects, ending with obj_foo,
     * your subsequent call can include before=obj_foo in order to fetch the previous page of the list.
     */
    String before;
    /**
     * Filter messages by the run ID that generated them.
     */
    @JsonProperty("run_id")
    private String runId;

    public enum Order {
        @JsonProperty("asc")
        ASCENDING,

        @JsonProperty("desc")
        DESCENDING
    }
}
