package com.theokanning.openai.embedding;

import java.util.List;

/**
 * @author LiangTao
 * @date 2024年05月07 16:36
 **/
public class ArrayOfArraysInput implements InputData {
    private List<List<Integer>> data;

    public ArrayOfArraysInput(List<List<Integer>> data) {
        this.data = data;
    }
}
