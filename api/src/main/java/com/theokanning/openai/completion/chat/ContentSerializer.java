package com.theokanning.openai.completion.chat;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Collection;

/**
 * @author LiangTao
 * @date 2024年04月10 11:39
 **/
public class ContentSerializer extends JsonSerializer<Object> {

    @Override
    public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (o instanceof String) {
            jsonGenerator.writeString((String) o);
        }

        if (o instanceof Collection) {
            jsonGenerator.writeStartArray();
            Collection oc = (Collection) o;
            for (Object item : oc) {
                if (item instanceof ImageContent){
                    ImageContent ic = (ImageContent) item;
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeStringField("type", ic.getType());
                    if (ic.getType().equals("text")) {
                        jsonGenerator.writeStringField("text", ic.getText());
                    }
                    if (ic.getType().equals("image_url")) {
                        jsonGenerator.writeObjectField("image_url", ic.getImageUrl());
                    }
                    if (ic.getType().equals("image_file")) {
                        jsonGenerator.writeObjectField("image_file", ic.getImageFile());
                    }
                    if (ic.getType().equals("input_audio")) {
                        jsonGenerator.writeObjectField("input_audio", ic.getInputAudio());
                    }
                    jsonGenerator.writeEndObject();
                }else if (item instanceof MultiMediaContent){
                    MultiMediaContent mmc = (MultiMediaContent) item;
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeStringField("type", mmc.getType());
                    if (mmc.getType().equals("text")) {
                        jsonGenerator.writeStringField("text", mmc.getText());
                    }
                    if (mmc.getType().equals("image_url")) {
                        jsonGenerator.writeObjectField("image_url", mmc.getImageUrl());
                    }
                    if (mmc.getType().equals("image_file")) {
                        jsonGenerator.writeObjectField("image_file", mmc.getImageFile());
                    }
                    if (mmc.getType().equals("input_audio")) {
                        jsonGenerator.writeObjectField("input_audio", mmc.getInputAudio());
                    }
                    jsonGenerator.writeEndObject();
                }
            }
            jsonGenerator.writeEndArray();
        }
    }
}
