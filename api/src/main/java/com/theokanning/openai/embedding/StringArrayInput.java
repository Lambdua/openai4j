package com.theokanning.openai.embedding;

import lombok.Data;

import java.util.List;

/**
 * @author LiangTao
 * @date 2024年05月07 16:35
 **/
@Data
public class StringArrayInput implements InputData {
    private List<String> data;

    public StringArrayInput(List<String> data) {
        this.data = data;
    }
}
