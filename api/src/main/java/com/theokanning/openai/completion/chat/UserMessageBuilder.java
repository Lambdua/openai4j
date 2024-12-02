package com.theokanning.openai.completion.chat;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author LiangTao
 * @date 2024年11月29 15:18
 **/
@Slf4j
public class UserMessageBuilder {

    private final UserMessage userMessage;

    public UserMessageBuilder() {
        userMessage = new UserMessage();
    }

    public UserMessageBuilder withName(String name) {
        userMessage.setName(name);
        return this;
    }

    /**
     * Set the text content of the message.
     * @param text the text content of the message
     */
    public UserMessageBuilder withTextMessage(String text) {
        userMessage.setContent(text);
        return this;
    }


    /**
     * build the message with image/audio  content
     * @param multiMediaContent the image content or audio content  of the message
     * @return the UserMessageBuilder
     */
    public UserMessageBuilder withMultiMediaContent(List<MultiMediaContent> multiMediaContent) {
        userMessage.setContent(multiMediaContent);
        return this;
    }


    /**
     * This method supports adding image/audio message content to assist the component UserMessage
     * @param multiMediaContents image audio message content
     */
    public UserMessageBuilder addMultiMediaContents(MultiMediaContent... multiMediaContents) {
        Object content = userMessage.getContent();
        if (content==null){
            ArrayList<MultiMediaContent> multiMediaContentList = new ArrayList<>();
            userMessage.setContent(multiMediaContentList);
        }else {
            if (content instanceof String){
                log.error("The content of the message is text, can not add image content");
                //maybe throw exception
                return this;
            }
        }
        List<MultiMediaContent> multiMediaContentList = (List<MultiMediaContent>) content;
        multiMediaContentList.addAll(Arrays.asList(multiMediaContents));
        return this;
    }

    /**
     * Used for component image messages, supporting multiple images
     * @param prompt  prompt
     * @param imageUrl image url or base64 image data
     */
    public UserMessage buildImageMessage(String prompt, String imageUrl) {
        return buildImageMessageWithDetail(prompt,"auto",imageUrl);
    }

    /**
     * Used for component image messages, supporting multiple images
     * @param prompt  prompt
     * @param detail level: "auto", "low", "high"
     * @param imageUrls image urls or base64 image data
     */
    public UserMessage buildImageMessageWithDetail(String prompt, String detail,String... imageUrls) {
        List<MultiMediaContent> imageContents = Arrays.stream(imageUrls).map(url -> MultiMediaContent.ofImageUrl(url,detail)).collect(Collectors.toList());
        imageContents.add(0, new MultiMediaContent(prompt));
        userMessage.setContent(imageContents);
        return build();
    }

    /**
     * Used for component image messages, supporting multiple images
     * @param prompt  prompt
     * @param imageUrls image urls or base64 image data
     */
    public UserMessage buildImageMessage(String prompt, String... imageUrls) {
        return buildImageMessageWithDetail(prompt,"auto",imageUrls);
    }

    /**
     * Used for component image messages, supporting multiple images
     * @param prompt prompt
     * @param detail level: "auto", "low", "high"
     * @param imagePaths image file paths
     */
    public UserMessage buildImageMessageWithDetail(String prompt, String detail, Path... imagePaths) {
        List<MultiMediaContent> imageContents = Arrays.stream(imagePaths).map(path -> MultiMediaContent.ofImagePath(path,detail)).collect(Collectors.toList());
        imageContents.add(0, new MultiMediaContent(prompt));
        userMessage.setContent(imageContents);
        return build();
    }


    /**
     * Used for component image messages, supporting multiple images
     * @param prompt prompt
     * @param imagePaths image file paths
     */
    public UserMessage buildImageMessage(String prompt, Path... imagePaths) {
        return buildImageMessageWithDetail(prompt,"auto",imagePaths);
    }

    /**
     * Used for component audio messages, supporting multiple audio
     * @param prompt prompt
     * @param audioPaths audio file paths
     */
    public UserMessage buildAudioMessage(String prompt, Path... audioPaths) {
        List<MultiMediaContent> audioContents = Arrays.stream(audioPaths).map(MultiMediaContent::ofAudioPath).collect(Collectors.toList());
        audioContents.add(0, new MultiMediaContent(prompt));
        userMessage.setContent(audioContents);
        return build();
    }



    public UserMessage build() {
        return userMessage;
    }


}
