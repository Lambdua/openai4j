package com.theokanning.openai.service;

import com.theokanning.openai.DeleteResult;
import com.theokanning.openai.ListSearchParameters;
import com.theokanning.openai.OpenAiResponse;
import com.theokanning.openai.assistants.assistant.*;
import com.theokanning.openai.utils.TikTokensUtil;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AssistantTest {

    static OpenAiService service = new OpenAiService(System.getenv("OPENAI_TOKEN"));
    static String assistantId;
    static String fileId;


    // @AfterAll
    // static void teardown() {
    //     try {
    //         service.deleteAssistantFile(assistantId, fileId);
    //     } catch (Exception e) {
    //         // do nothing
    //     }
    //     try {
    //         service.deleteAssistant(assistantId);
    //     } catch (Exception e) {
    //         // do nothing
    //     }
    // }

    @Test
    @Order(1)
    void createAssistant() {
        AssistantRequest assistantRequest = AssistantRequest.builder().model(TikTokensUtil.ModelEnum.GPT_4_1106_preview.getName()).name("Math Tutor").instructions("You are a personal Math Tutor.").tools(Collections.singletonList(new CodeInterpreterTool())).build();
        Assistant assistant = service.createAssistant(assistantRequest);

        assistantId = assistant.getId();

        assertEquals(assistant.getName(), "Math Tutor");
        assertEquals(assistant.getTools().get(0).getType(), AssistantToolsEnum.CODE_INTERPRETER);
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
        ModifyAssistantRequest modifyRequest = ModifyAssistantRequest.builder().name(modifiedName).build();

        Assistant modifiedAssistant = service.modifyAssistant(assistantId, modifyRequest);
        assertEquals(modifiedName, modifiedAssistant.getName());
    }

    @Test
    @Order(4)
    void listAssistants() {
        OpenAiResponse<Assistant> assistants = service.listAssistants(ListSearchParameters.builder().build());

        assertNotNull(assistants);
        assertFalse(assistants.getData().isEmpty());
    }

    // @Test
    // @Order(5)
    // void createAssistantFile() {
    //     String filePath = "src/test/resources/assistants-data.html";
    //     File uploadedFile = service.uploadFile("assistants", filePath);
    //
    //     AssistantFile assistantFile = service.createAssistantFile(assistantId, new VectoreStoreFileRequest(uploadedFile.getId()));
    //
    //     fileId = assistantFile.getId();
    //     assertNotNull(assistantFile);
    //     assertEquals(uploadedFile.getId(), assistantFile.getId());
    //     assertEquals(assistantId, assistantFile.getAssistantId());
    // }
    //
    // @Test
    // @Order(6)
    // void retrieveAssistantFile() {
    //     AssistantFile file = service.retrieveAssistantFile(assistantId, fileId);
    //
    //     assertEquals(file.getId(), fileId);
    // }


    // @Test
    // @Order(7)
    // void listAssistantFiles() {
    //     List<AssistantFile> files = service.listAssistantFiles(assistantId, new ListSearchParameters()).data;
    //
    //     assertFalse(files.isEmpty());
    //     assertEquals(files.get(0).getId(), fileId);
    //     assertEquals(files.get(0).getObject(), "assistant.file");
    // }
    //
    // @Test
    // @Order(8)
    // void deleteAssistantFile() {
    //     DeleteResult deletedFile = service.deleteAssistantFile(assistantId, fileId);
    //
    //     assertEquals(deletedFile.getId(), fileId);
    //     assertTrue(deletedFile.isDeleted());
    // }

    @Test
    @Order(9)
    void deleteAssistant() {
        DeleteResult deletedAssistant = service.deleteAssistant(assistantId);

        assertEquals(assistantId, deletedAssistant.getId());
        assertTrue(deletedAssistant.isDeleted());
    }
}
