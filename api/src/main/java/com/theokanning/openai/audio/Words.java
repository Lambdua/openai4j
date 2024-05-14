package com.theokanning.openai.audio;

import lombok.Data;

/**
 * @author LiangTao
 * @date 2024年05月14 09:56
 **/
@Data
public class Words {
    private String word;

    private Double start;

    private Double end;
}
