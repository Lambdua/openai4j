package com.theokanning.openai.runs;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * @author LiangTao
 * @date 2024年04月18 17:13
 **/
public class ToolChoiceDeserializer extends JsonDeserializer<Object> {

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        if (jsonParser.getCurrentToken() == JsonToken.VALUE_STRING) {
            return jsonParser.getText();
        }
        if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
            // 处理对象的情况
            ToolChoice toolChoice = new ToolChoice();
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                // 判断对象内元素类型并进行相应的反序列化
                if (jsonParser.getCurrentName().equals("type")) {
                    toolChoice.setType(jsonParser.nextTextValue());
                }
                if (jsonParser.getCurrentName().equals("function")) {
                    toolChoice.setFunction(parseFunction(jsonParser));
                }
            }
            return toolChoice;
        }
        //抛出异常
        return null;
    }

    private Function parseFunction(JsonParser jsonParser) throws IOException {
        if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
            // 处理对象的情况
            Function function = new Function();
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                // 判断对象内元素类型并进行相应的反序列化
                if (jsonParser.getCurrentName().equals("name")) {
                    function.setName(jsonParser.nextTextValue());
                }
            }
            return function;
        }
        //抛出异常
        return null;
    }
}
