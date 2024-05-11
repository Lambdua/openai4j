package com.theokanning.openai.service;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @deprecated This class is deprecated and will be removed in a future version
 */
@Deprecated
public abstract class ChatFunctionMixIn {

    @JsonSerialize(using = ChatFunctionParametersSerializer.class)
    abstract Class<?> getParametersClass();

}
