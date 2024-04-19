package com.theokanning.openai.assistants.assistant;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum AssistantSortOrder {

    @JsonProperty("asc")
    ASC,

    @JsonProperty("desc")
    DESC
}
