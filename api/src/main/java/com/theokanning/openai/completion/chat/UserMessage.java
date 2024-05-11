package com.theokanning.openai.completion.chat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author LiangTao
 * @date 2024年04月10 10:17
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMessage implements ChatMessage {
    private final String role = ChatMessageRole.USER.value();

    @JsonDeserialize(using = ContentDeserializer.class)
    @JsonSerialize(using = ContentSerializer.class)
    private Object content;

    //An optional name for the participant. Provides the model information to differentiate between participants of the same role.
    private String name;

    public UserMessage(String content) {
        this.content = content;
    }

    public UserMessage(List<ImageContent> imageContents) {
        this.content = imageContents;
    }




    @Override
    @JsonIgnore
    public String getTextContent() {
        if (content instanceof String) {
            return (String) content;
        }
        if (content instanceof Collection) {
            Collection collection = (Collection) content;
            Optional<String> text = collection.stream().filter(item -> item instanceof ImageContent)
                    .filter(imageContent -> ((ImageContent) imageContent).getType().equals("text"))
                    .findFirst().map(imageContent -> ((ImageContent) imageContent).getText());
            if (text.isPresent()) {
                return text.get();
            }
        }
        return null;
    }

    /**
     * 构件一个图片识别请求消息,支持多个图片
     *
     * @param text      query text
     * @param imageUrls image urls
     * @return com.theokanning.openai.completion.chat.UserMessage
     * @author liangtao
     * @date 2024/4/12
     **/
    public static UserMessage buildImageMessage(String text, String... imageUrls) {
        List<ImageContent> imageContents = Arrays.stream(imageUrls).map(url -> new ImageContent(new ImageUrl(url))).collect(Collectors.toList());
        imageContents.add(0, new ImageContent(text));
        return new UserMessage(imageContents);
    }
}

