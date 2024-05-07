package com.theokanning.openai.embedding;

import java.util.List;

/**
 * @author LiangTao
 * @date 2024年05月07 16:36
 **/
public class IntegerArrayInput implements InputData {
    private List<Integer> data;

    public IntegerArrayInput(List<Integer> data) {
        this.data = data;
    }
}
