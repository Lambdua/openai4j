package com.theokanning.openai.assistants.run;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.theokanning.openai.assistants.assistant.Tool;
import com.theokanning.openai.assistants.message.MessageRequest;
import com.theokanning.openai.completion.chat.ChatResponseFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RunCreateRequest {
    @JsonProperty("assistant_id")
    String assistantId;

    /**
     * The ID of the Model to be used to execute this run.
     * If a value is provided here, it will override the model associated with the assistant.
     * If not, the model associated with the assistant will be used.
     */
    String model;

    /**
     * Overrides the instructions of the assistant. This is useful for modifying the behavior on a per-run basis.
     */
    String instructions;

    /**
     * Appends additional instructions at the end of the instructions for the run. This is useful for modifying the behavior on a per-run basis without overriding other instructions.
     */
    @JsonProperty("additional_instructions")
    String additionalInstructions;

    /**
     * Adds additional messages to the thread before creating the run.
     */
    @JsonProperty("additional_messages")
    List<MessageRequest> additionalMessages;

    /**
     * Override the tools the assistant can use for this run. This is useful for modifying the behavior on a per-run basis.
     */
    List<Tool> tools;

    Map<String, String> metadata;


    /**
     * What sampling temperature to use, between 0 and 2. Higher values like 0.8 will make the output more random, while lower values like 0.2 will make it more focused and deterministic.
     */
    Double temperature;

    /**
     * An alternative to sampling with temperature, called nucleus sampling, where the model considers the results of the tokens with top_p probability mass. So 0.1 means only the tokens comprising the top 10% probability mass are considered.
     * We generally recommend altering this or temperature but not both.
     */
    @JsonProperty("top_p")
    Double topP;

    /**
     * If true, returns a stream of events that happen during the Run as server-sent events, terminating when the Run enters a terminal state with a data: [DONE] message
     */
    Boolean stream;

    /**
     * The maximum number of prompt tokens that may be used over the course of the run.
     * The run will make a best effort to use only the number of prompt tokens specified, across multiple turns of the run.
     * If the run exceeds the number of prompt tokens specified, the run will end with status incomplete. See incomplete_details for more info.
     */
    @JsonProperty("max_prompt_tokens")
    Integer maxPromptTokens;

    /**
     * The maximum number of completion tokens that may be used over the course of the run.
     * The run will make a best effort to use only the number of completion tokens specified, across multiple turns of the run.
     * If the run exceeds the number of completion tokens specified, the run will end with status incomplete. See incomplete_details for more info.
     */
    @JsonProperty("max_completion_tokens")
    Integer maxCompletionTokens;

    /**
     * Controls for how a thread will be truncated prior to the run. Use this to control the intial context window of the run.
     */
    @JsonProperty("truncation_strategy")
    TruncationStrategy truncationStrategy;

    /**
     * Controls which (if any) tool is called by the model.
     * none means the model will not call any tools and instead generates a message.
     * auto is the default value and means the model can pick between generating a message or calling a tool.
     * Specifying a particular tool like {"type": "file_search"} or {"type": "function", "function": {"name": "my_function"}} forces the model to call that tool.
     */
    @JsonProperty("tool_choice")
    @JsonSerialize(using = ToolChoice.Serializer.class)
    @JsonDeserialize(using = ToolChoice.Deserializer.class)
    ToolChoice toolChoice;

    /**
     * Specifies the format that the model must output. Compatible with GPT-4 Turbo and all GPT-3.5 Turbo models since gpt-3.5-turbo-1106.
     * Setting to { "type": "json_object" } enables JSON mode, which guarantees the message the model generates is valid JSON.
     * <p>
     * Important: when using JSON mode, you must also instruct the model to produce JSON yourself via a system or user message.
     * Without this, the model may generate an unending stream of whitespace until the generation reaches the token limit, resulting in a long-running and seemingly "stuck" request.
     * Also note that the message content may be partially cut off if finish_reason="length", which indicates the generation exceeded max_tokens or the conversation exceeded the max context length.
     * <p>
     */
    @JsonProperty("response_format")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = ChatResponseFormat.ChatResponseFormatSerializer.class)
    @JsonDeserialize(using = ChatResponseFormat.ChatResponseFormatDeserializer.class)
    ChatResponseFormat responseFormat;
}
