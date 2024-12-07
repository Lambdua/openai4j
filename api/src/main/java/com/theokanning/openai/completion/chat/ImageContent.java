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
 * @deprecated use {@link MultiMediaContent},the name is not accurate,use new class {@link MultiMediaContent} instead and the refactoring is done
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Deprecated
public class ImageContent {

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
    
    public ImageContent(ImageFile imageFile) {
        this.type = "image_file";
        this.imageFile = imageFile;
    }

    public ImageContent(String text) {
        this.type = "text";
        this.text = text;
    }

    public ImageContent(ImageUrl imageUrl) {
        this.type = "image_url";
        this.imageUrl = imageUrl;
    }

    /**
     * @deprecated {@link #ofImagePath(Path)}
     */
    @Deprecated
    public ImageContent(Path imagePath){
        this.type = "image_url";
        String imagePathString = imagePath.toAbsolutePath().toString();
        String extension = imagePathString.substring(imagePathString.lastIndexOf('.') + 1);
        this.imageUrl=new ImageUrl( "data:image/" + extension + ";base64," + encodeImage(imagePath));
    }

    public ImageContent(InputAudio inputAudio) {
        this.type = "input_audio";
        this.inputAudio = inputAudio;
    }

    public static ImageContent ofImagePath(Path imagePath){
        String imagePathString = imagePath.toAbsolutePath().toString();
        String extension = imagePathString.substring(imagePathString.lastIndexOf('.') + 1);
        ImageUrl imageUrl = new ImageUrl("data:image/" + extension + ";base64," + encode2base64(imagePath));
        return new ImageContent(imageUrl);
    }

    public static ImageContent ofAudioPath(Path inputAudioPath) {
        String inputAudioPathString = inputAudioPath.toAbsolutePath().toString();
        String extension = inputAudioPathString.substring(inputAudioPathString.lastIndexOf('.') + 1);
        String base64 = encode2base64(inputAudioPath);
        InputAudio inputAudio = new InputAudio(base64, extension);
        return new ImageContent(inputAudio);
    }

    /**
     * @deprecated use {@link #encode2base64(Path)}
     */
    @Deprecated
    private static String encodeImage(Path imagePath) {
        byte[] fileContent;
        try {
            fileContent = Files.readAllBytes(imagePath);
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
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
