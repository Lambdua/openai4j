package com.theokanning.openai.completion.chat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * system message
 *
 * @author LiangTao
 * @date 2024年04月10 10:13
 **/

@Data
@NoArgsConstructor
public class SystemMessage implements ChatMessage {
    @NonNull
    final String role = ChatMessageRole.SYSTEM.value();


    // content should always exist in the call, even if it is null
    @JsonInclude()
    String content;

    //An optional name for the participant. Provides the model information to differentiate between participants of the same role.
    String name;


    public SystemMessage(String content) {
        this.content = content;
    }

    public SystemMessage(String content, String name) {
        this.content = content;
        this.name = name;
    }

    @Override
    @JsonIgnore
    public String getTextContent() {
        return content;
    }
}
