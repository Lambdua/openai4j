package com.theokanning.openai.service.assistants;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.DeleteResult;
import com.theokanning.openai.ListSearchParameters;
import com.theokanning.openai.OpenAiResponse;
import com.theokanning.openai.assistants.assistant.*;
import com.theokanning.openai.completion.chat.ChatFunctionDynamic;
import com.theokanning.openai.completion.chat.ChatFunctionProperty;
import com.theokanning.openai.completion.chat.ChatResponseFormat;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.utils.TikTokensUtil;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;


/**
 * code interpreter assistant test
 *
 * @author LiangTao
 * @date 2024年04月26 15:17
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AssistantTest {

    static OpenAiService service = new OpenAiService();

    static ObjectMapper mapper = new ObjectMapper();
    static String assistantId;

    @AfterAll
    static void teardown() {
        try {
            DeleteResult deleteResult = service.deleteAssistant(assistantId);
            assertTrue(deleteResult.isDeleted());
        } catch (Exception e) {
            // ignore
        }
    }

    @Test
    @Order(1)
    void createAssistant() throws JsonProcessingException {
        ChatFunctionDynamic dynamicFun = ChatFunctionDynamic.builder()
                .name("weather_reporter")
                .description("Get the current weather of a location")
                .addProperty(ChatFunctionProperty.builder()
                        .name("location")
                        .type("string")
                        .description("The city and state, e.g. San Francisco, CA")
                        .build())
                .addProperty(ChatFunctionProperty.builder()
                        .name("unit")
                        .type("string")
                        .enumValues(new HashSet<>(Arrays.asList("celsius", "fahrenheit")))
                        .description("The temperature unit, can be 'celsius' or 'fahrenheit")
                        .required(true)
                        .build()).build();

        AssistantRequest assistantRequest = AssistantRequest.builder()
                .model("gpt-3.5-turbo")
                .name("Math Tutor")
                .description("the personal Math Tutor")
                .instructions("You are a personal Math Tutor.")
                .tools(Arrays.asList(
                        new CodeInterpreterTool(),
                        new FunctionTool(dynamicFun)
                ))
                .responseFormat(ChatResponseFormat.AUTO)
                .temperature(0.2D)
                .build();
        Assistant assistant = service.createAssistant(assistantRequest);
        assistantId = assistant.getId();
        assertEquals(assistant.getName(), "Math Tutor");
        assertEquals(assistant.getTools().get(0).getType(), "code_interpreter");
        assertEquals(assistant.getTools().get(1).getType(), "function");
        assertEquals(assistant.getTemperature(), 0.2D);
        assertEquals(assistant.getResponseFormat(), ChatResponseFormat.AUTO);
        assertEquals(assistant.getInstructions(), "You are a personal Math Tutor.");
        assertEquals(assistant.getModel(), TikTokensUtil.ModelEnum.GPT_3_5_TURBO.getName());
        assertEquals(assistant.getDescription(), "the personal Math Tutor");
    }

    @Test
    @Order(2)
    void retrieveAssistant() {
        Assistant assistant = service.retrieveAssistant(assistantId);
        assertEquals(assistant.getName(), "Math Tutor");
    }

    @Test
    @Order(3)
    void modifyAssistant() {
        String modifiedName = "Science Tutor";
        ModifyAssistantRequest modifyRequest = ModifyAssistantRequest.builder()
                .name(modifiedName)
                .temperature(1D)
                .description("the personal Science Tutor")
                .build();
        Assistant modifiedAssistant = service.modifyAssistant(assistantId, modifyRequest);
        assertEquals(modifiedName, modifiedAssistant.getName());
        assertEquals(1D, modifiedAssistant.getTemperature());
        assertEquals("the personal Science Tutor", modifiedAssistant.getDescription());
    }

    @Test
    @Order(4)
    void listAssistants() {
        OpenAiResponse<Assistant> assistants = service.listAssistants(ListSearchParameters.builder().build());
        assertNotNull(assistants);
        assertFalse(assistants.getData().isEmpty());
    }

    @Test
    @Order(9)
    void deleteAssistant() {
        DeleteResult deletedAssistant = service.deleteAssistant(assistantId);
        assertEquals(assistantId, deletedAssistant.getId());
        assertTrue(deletedAssistant.isDeleted());
    }
}
