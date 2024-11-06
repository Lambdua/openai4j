package com.theokanning.openai.completion.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * @author Allen Hu
 * @date 2024/11/6
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InputAudio {

    /**
     * Base64 encoded audio data.
     */
    @NonNull
    private String data;

    /**
     * The format of the encoded audio data. Currently supports "wav" and "mp3".
     */
    @NonNull
    private String format;
}
