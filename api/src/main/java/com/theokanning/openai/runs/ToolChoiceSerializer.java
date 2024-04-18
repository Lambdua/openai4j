package com.theokanning.openai.runs;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * @author LiangTao
 * @date 2024年04月18 17:13
 **/
public class ToolChoiceSerializer extends JsonSerializer<Object> {
    //    /**
    //      * Controls which (if any) tool is called by the model.
    //      * none means the model will not call any tools and instead generates a message.
    //      * auto is the default value and means the model can pick between generating a message or calling a tool.
    //      * Specifying a particular tool like {"type": "file_search"} or {"type": "function", "function": {"name": "my_function"}} forces the model to call that tool.
    //      */


    @Override
    public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (o instanceof String) {
            jsonGenerator.writeString((String) o);
        }
        if (o instanceof ToolChoice) {
            ToolChoice toolChoice = (ToolChoice) o;
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("type", toolChoice.getType());
            if (toolChoice.getType().equals("function")) {
                jsonGenerator.writeObjectField("function", toolChoice.getFunction());
            }
            jsonGenerator.writeEndObject();
        }
    }
}
