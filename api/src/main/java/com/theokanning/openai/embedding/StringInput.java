package com.theokanning.openai.embedding;

import lombok.Data;

/**
 * @author LiangTao
 * @date 2024年05月07 16:33
 **/
@Data
public class StringInput implements InputData {
    private String data;

    public StringInput(String data) {
        this.data = data;
    }
}
