package com.theokanning.openai.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2023-11-16 22:27
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LastError {

    /**
     * One of server_error or rate_limit_exceeded.
     */
    private String code;

    private String message;
}
