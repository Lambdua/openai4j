package com.theokanning.openai.completion.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * @author LiangTao
 * @date 2024年04月10 10:26
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageUrl {
    @NonNull
    private String url;

    private String detail;

    public ImageUrl(String url) {
        this.url = url;
    }


}
