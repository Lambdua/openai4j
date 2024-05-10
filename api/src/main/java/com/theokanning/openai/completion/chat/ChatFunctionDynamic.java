package com.theokanning.openai.completion.chat;

import lombok.Data;
import lombok.NonNull;


/**
 * @deprecated function json schema(https://json-schema.org/understanding-json-schema/reference) 十分复杂,很多属性都是可选的,维护这个类会变得异常复杂,目前仅仅只是支持了很简单的属性,肯定无法满足更多灵活的function定义.<br>
 * 现在推荐使用{@link  ChatFunction}来定义function
 *
 */
@Data
@Deprecated
public class ChatFunctionDynamic {

    /**
     * The name of the function being called.
     */
    @NonNull
    private String name;

    /**
     * A description of what the function does, used by the model to choose when and how to call the function.
     */
    private String description;

    /**
     * The parameters the functions accepts.
     */
    private ChatFunctionParameters parameters;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String description;
        private ChatFunctionParameters parameters = new ChatFunctionParameters();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder parameters(ChatFunctionParameters parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder addProperty(ChatFunctionProperty property) {
            this.parameters.addProperty(property);
            return this;
        }

        public ChatFunctionDynamic build() {
            ChatFunctionDynamic chatFunction = new ChatFunctionDynamic(name);
            chatFunction.setDescription(description);
            chatFunction.setParameters(parameters);
            return chatFunction;
        }
    }
}
