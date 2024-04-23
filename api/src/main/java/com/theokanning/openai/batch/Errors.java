package com.theokanning.openai.batch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author LiangTao
 * @date 2024年04月23 13:46
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Errors {
    /**
     * The object type, which is always list.
     */
    String object;

    List<ErrorsData> data;
}
