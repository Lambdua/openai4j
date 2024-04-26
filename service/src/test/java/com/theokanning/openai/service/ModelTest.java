package com.theokanning.openai.service;

import com.theokanning.openai.model.Model;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


public class ModelTest {

    com.theokanning.openai.service.OpenAiService service = new OpenAiService();

    @Test
    void listModels() {
        List<Model> models = service.listModels();

        assertFalse(models.isEmpty());
    }

    @Test
    void getModel() {
        Model model = service.getModel("babbage-002");

        assertEquals("babbage-002", model.id);
        assertEquals("system", model.ownedBy);
    }
}
