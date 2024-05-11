package com.theokanning.openai.completion.chat;

import lombok.Data;
import lombok.NonNull;

/**
 * @author LiangTao
 * @date 2024年04月10 10:26
 **/
@Data
public class ImageUrl {
    @NonNull
    private final String url;

    private String detail;

    public ImageUrl(String url) {
        this.url = url;
    }
}
