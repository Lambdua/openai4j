package com.theokanning.openai.completion.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Parameters for audio output. Required when audio output is requested with modalities: ["audio"]
 *
 * @author Allen Hu
 * @date 2024/11/5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Audio {

    /**
     * The voice the model uses to respond. Supported voices are alloy, ash, ballad, coral, echo, sage, shimmer, and verse.
     */
    String voice;

    /**
     * Specifies the output audio format. Must be one of wav, mp3, flac, opus, or pcm16.
     */
    String format;
}
