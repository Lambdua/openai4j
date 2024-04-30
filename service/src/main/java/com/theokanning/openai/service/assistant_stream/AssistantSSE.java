package com.theokanning.openai.service.assistant_stream;

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

    public AssistantSSE(StreamEvent event, String data) {
        this.event = event;
        this.data = data;
    }


    public boolean isDone() {
        return event.equals(StreamEvent.DONE);
    }

    @Override
    public String toString() {
        return "AssistantSSE{" +
                "event=" + event.eventName +
                ", data='" + data + '\'' +
                '}';
    }
}
