package com.theokanning.openai.service.assistant_stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.theokanning.openai.assistants.message.content.DeltaContent;
import com.theokanning.openai.assistants.message.content.MessageDelta;
import com.theokanning.openai.assistants.message.content.Text;
import com.theokanning.openai.assistants.run.ToolCall;
import com.theokanning.openai.assistants.run.ToolCallCodeInterpreter;
import com.theokanning.openai.assistants.run.ToolCallFunction;
import com.theokanning.openai.assistants.run_step.RunStepDelta;
import com.theokanning.openai.assistants.run_step.StepDetails;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Optional;

/**
 * @author LiangTao
 * @date 2024年05月02 15:56
 **/
public class DeltaUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * merge delta msg to accumulated delta msg
     *
     * @return com.theokanning.openai.assistants.message.content.MessageDelta
     * @author liangtao
     * @date 2024/5/2
     **/
    @SneakyThrows({JsonProcessingException.class})
    public static MessageDelta accumulatMessageDelta(MessageDelta accumulated, MessageDelta nowDelta) {
        if (accumulated == null) {
            //use json to clone
            return mapper.readValue(mapper.writeValueAsString(nowDelta), MessageDelta.class);
        }
        MessageDelta result = mapper.readValue(mapper.writeValueAsString(accumulated), MessageDelta.class);

        //merge
        DeltaContent nowDeltaContent = nowDelta.getDelta().getContent().get(0);
        List<DeltaContent> preContent = result.getDelta().getContent();
        Optional<DeltaContent> existsCurrent = preContent.stream().filter(c -> c.getIndex().equals(nowDeltaContent.getIndex())).findFirst();
        if (existsCurrent.isPresent()) {
            DeltaContent existsContent = existsCurrent.get();
            if (nowDeltaContent.getType().equals("text")) {
                Text text = existsContent.getText();
                text.setValue(text.getValue() + nowDeltaContent.getText().getValue());
                if (nowDeltaContent.getText().getAnnotations() != null && !nowDeltaContent.getText().getAnnotations().isEmpty()) {
                    if (text.getAnnotations() == null) {
                        text.setAnnotations(nowDeltaContent.getText().getAnnotations());
                    } else {
                        text.getAnnotations().addAll(nowDeltaContent.getText().getAnnotations());
                    }
                }
            }
            if (nowDeltaContent.getType().equals("image_file") && nowDeltaContent.getImageFile() != null) {
                existsContent.setImageFile(nowDeltaContent.getImageFile());
            }
            if (nowDeltaContent.getType().equals("image_url") && nowDeltaContent.getImageUrl() != null) {
                existsContent.setImageUrl(nowDeltaContent.getImageUrl());
            }
        } else {
            result.getDelta().getContent().add(nowDeltaContent);
        }
        return result;
    }

    @SneakyThrows({JsonProcessingException.class})
    public static RunStepDelta accumulatRunStepDelta(RunStepDelta accumulatedRsd, RunStepDelta nowRSD) {
        if (accumulatedRsd == null) {
            //use json to clone
            return mapper.readValue(mapper.writeValueAsString(nowRSD), RunStepDelta.class);
        }
        RunStepDelta result = mapper.readValue(mapper.writeValueAsString(accumulatedRsd), RunStepDelta.class);

        StepDetails currentDetails = nowRSD.getDelta().getStepDetails();
        ToolCall currentToolCallPart = currentDetails.getToolCalls().get(0);
        StepDetails preDetails = result.getDelta().getStepDetails();
        Optional<ToolCall> existsToolCallOptional = preDetails.getToolCalls().stream().filter(t -> t.getIndex().equals(currentToolCallPart.getIndex())).findFirst();
        if (!existsToolCallOptional.isPresent()) {
            preDetails.setToolCalls(currentDetails.getToolCalls());
            return result;
        }

        ToolCall existsToolCallPart = existsToolCallOptional.get();
        if (existsToolCallPart.getType().equals("function")) {
            ToolCallFunction currentFunPart = currentToolCallPart.getFunction();
            ToolCallFunction existsFunPart = existsToolCallPart.getFunction();
            if (currentFunPart.getName() != null && !currentFunPart.getName().isEmpty()) {
                existsFunPart.setName(Optional.ofNullable(existsFunPart.getName()).orElse("") + currentFunPart.getName());
            }
            if (currentFunPart.getArguments() != null) {
                existsFunPart.setArguments(new TextNode(Optional.ofNullable(existsFunPart.getArguments()).orElse(new TextNode("")).asText() + currentFunPart.getArguments().asText()));
            }
        } else if (existsToolCallPart.getType().equals("file_search")) {
            //file_search类型的数据不需要合并,只返回一次
        } else if (existsToolCallPart.getType().equals("code_interpreter")) {
            ToolCallCodeInterpreter currentCodeInterpreter = currentToolCallPart.getCodeInterpreter();
            ToolCallCodeInterpreter existsCodeInterpreter = existsToolCallPart.getCodeInterpreter();
            if (currentCodeInterpreter.getInput() != null) {
                existsCodeInterpreter.setInput(existsCodeInterpreter.getInput() + currentCodeInterpreter.getInput());
            }
            if (currentCodeInterpreter.getOutputs() != null) {
                if (existsCodeInterpreter.getOutputs() == null) {
                    existsCodeInterpreter.setOutputs(currentCodeInterpreter.getOutputs());
                } else {
                    existsCodeInterpreter.getOutputs().addAll(currentCodeInterpreter.getOutputs());
                }
            }
        }
        return result;
    }

}
