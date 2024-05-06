package com.theokanning.openai.service.assistant_stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.assistants.StreamEvent;
import lombok.Getter;

/**
 * @author LiangTao
 * @date 2024年04月29 10:07
 **/
@Getter
public class AssistantSSE {
    private StreamEvent event;
    private String data;

    private static final ObjectMapper mapper = new ObjectMapper();


    public AssistantSSE(StreamEvent event, String data) {
        this.event = event;
        this.data = data;
    }


    public boolean isDone() {
        return event.equals(StreamEvent.DONE);
    }

    public <T> T getPojo() {
        try {
            return (T) mapper.readValue(data, event.dataClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "AssistantSSE{" +
                "event=" + event.eventName +
                ", data='" + data + '\'' +
                '}';
    }
}
