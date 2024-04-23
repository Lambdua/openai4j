package com.theokanning.openai.batch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LiangTao
 * @date 2024年04月23 13:50
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorsData {
    /**
     * An error code identifying the error type.
     */
    String code;

    /**
     * A human-readable message providing more details about the error.
     */
    String message;

    /**
     * The name of the parameter that caused the error, if applicable.
     */
    String param;

    /**
     * The line number of the input file where the error occurred, if applicable.
     */
    Integer line;
}
