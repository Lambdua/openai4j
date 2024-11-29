package com.theokanning.openai.completion.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.theokanning.openai.assistants.message.content.ImageFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

/**
 * @author LiangTao
 * @date 2024年04月10 10:26
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MultiMediaContent {

    /**
     * The type of the content. Either "text", "image_url" or "input_audio".
     */
    @NonNull
    private String type;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String text;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("image_url")
    private ImageUrl imageUrl;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("image_file")
    private ImageFile imageFile;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("input_audio")
    private InputAudio inputAudio;

    public MultiMediaContent(String text) {
        this.type = "text";
        this.text = text;
    }

    public MultiMediaContent(ImageUrl imageUrl) {
        this.type = "image_url";
        this.imageUrl = imageUrl;
    }

    public MultiMediaContent(InputAudio inputAudio) {
        this.type = "input_audio";
        this.inputAudio = inputAudio;
    }

    /**
     * build an image content from a file path, detail is auto
     * @author liangtao
     * @date 2024/11/28
     * @param imagePath
     * @return com.theokanning.openai.completion.chat.ImageContent
     **/
    public static MultiMediaContent ofImagePath(Path imagePath){
        return ofImagePath(imagePath,"auto");
    }

    /**
     * build an image content from a file path,Specify detail
     * @author liangtao
     * @date 2024/11/28
     * @param imagePath
     * @param detail level: "auto", "low", "high"
     * @return com.theokanning.openai.completion.chat.ImageContent
     **/
    public static MultiMediaContent ofImagePath(Path imagePath, String detail){
        String imagePathString = imagePath.toAbsolutePath().toString();
        String extension = imagePathString.substring(imagePathString.lastIndexOf('.') + 1);
        ImageUrl imageUrl = new ImageUrl("data:image/" + extension + ";base64," + encode2base64(imagePath),detail);
        return new MultiMediaContent(imageUrl);
    }

    /**
     * build an image content from a url
     * @param imageUrl url
     * @param detail level: "auto", "low", "high"
     */
    public static MultiMediaContent ofImageUrl(String imageUrl, String detail) {
        return new MultiMediaContent(new ImageUrl(imageUrl, detail));
    }

    public static MultiMediaContent ofImageUrl(String imageUrl) {
        return ofImageUrl(imageUrl, "auto");
    }



    public static MultiMediaContent ofAudioPath(Path inputAudioPath) {
        String inputAudioPathString = inputAudioPath.toAbsolutePath().toString();
        String extension = inputAudioPathString.substring(inputAudioPathString.lastIndexOf('.') + 1);
        String base64 = encode2base64(inputAudioPath);
        InputAudio inputAudio = new InputAudio(base64, extension);
        return new MultiMediaContent(inputAudio);
    }


    private static String encode2base64(Path path) {
        byte[] fileContent;
        try {
            fileContent = Files.readAllBytes(path);
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
