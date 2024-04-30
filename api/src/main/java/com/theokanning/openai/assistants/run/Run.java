package com.theokanning.openai.assistants.run;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.theokanning.openai.Usage;
import com.theokanning.openai.assistants.assistant.Tool;
import com.theokanning.openai.assistants.assistant.ToolResources;
import com.theokanning.openai.assistants.message.IncompleteDetails;
import com.theokanning.openai.common.LastError;
import com.theokanning.openai.completion.chat.ChatResponseFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Run {

    private String id;

    private String object;

    @JsonProperty("created_at")
    private Integer createdAt;

    @JsonProperty("thread_id")
    private String threadId;

    @JsonProperty("assistant_id")
    private String assistantId;

    /**
     * The status of the run, which can be either queued, in_progress, requires_action, cancelling, cancelled, failed, completed, or expired.
     * <img src="https://cdn.openai.com/API/docs/images/diagram-run-statuses-v2.png" alt="run status">
     */
    private String status;

    /**
     * Details on the action required to continue the run. Will be null if no action is required.
     */
    @JsonProperty("required_action")
    private RequiredAction requiredAction;

    @JsonProperty("last_error")
    private LastError lastError;

    @JsonProperty("expires_at")
    private Integer expiresAt;

    @JsonProperty("started_at")
    private Integer startedAt;

    @JsonProperty("cancelled_at")
    private Integer cancelledAt;

    @JsonProperty("failed_at")
    private Integer failedAt;

    @JsonProperty("completed_at")
    private Integer completedAt;

    @JsonProperty("incomplete_details")
    IncompleteDetails incompleteDetails;

    private String model;

    private String instructions;

    private List<Tool> tools;

    private Map<String, String> metadata;

    Usage usage;

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


    /**
     * assistant stream event{@link com.theokanning.openai.assistants.StreamEvent#THREAD_RUN_CREATED} will return this field
     */
    @JsonProperty(value = "tool_resources")
    ToolResources toolResources;
}
