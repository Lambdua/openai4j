package com.theokanning.openai.completion.chat;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @deprecated function json schema(https://json-schema.org/understanding-json-schema/reference) 十分复杂,很多属性都是可选的,维护会变得异常复杂,目前仅仅只是支持了很简单的属性,肯定无法满足更多灵活的function定义.<br>
 * 并且将这个类放在了错误的包下,应该放在service包下.<br>
 */
@Data
@Deprecated
public class ChatFunctionParameters {

    private final String type = "object";

    private final HashMap<String, ChatFunctionProperty> properties = new HashMap<>();

    private List<String> required;

    public void addProperty(ChatFunctionProperty property) {
        properties.put(property.getName(), property);
        if (Boolean.TRUE.equals(property.getRequired())) {
            if (this.required == null) {
                this.required = new ArrayList<>();
            }
            this.required.add(property.getName());
        }
    }
}
