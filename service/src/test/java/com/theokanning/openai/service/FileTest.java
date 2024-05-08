package com.theokanning.openai.service;

import com.theokanning.openai.DeleteResult;
import com.theokanning.openai.file.File;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FileTest {

    OpenAiService service = new OpenAiService();

    @Test
    @Order(1)
    void uploadFile() throws Exception {
        String filePath = "src/test/resources/fine-tuning-data.jsonl";
        File file = service.uploadFile("fine-tune", filePath);
        String fileId = file.getId();
        assertEquals("fine-tune", file.getPurpose());
        assertEquals("fine-tuning-data.jsonl", file.getFilename());
        // wait for file to be processed
        TimeUnit.SECONDS.sleep(10);

        List<File> files = service.listFiles();
        assertTrue(files.stream().anyMatch(fileItem -> fileItem.getId().equals(fileId)));
        file = service.retrieveFile(fileId);
        assertEquals("fine-tuning-data.jsonl", file.getFilename());
        String fileBytesToString = service.retrieveFileContent(fileId).string();
        String contents = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        assertEquals(contents, fileBytesToString);
        DeleteResult result = service.deleteFile(fileId);
        assertTrue(result.isDeleted());
    }

    @Test
    @Order(2)
    void uploadFileStream() throws Exception {
        String filePath = "batch-task-data.jsonl";
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(filePath);
        File file = service.uploadFile("fine-tune", resourceAsStream, "batch-task-data.jsonl");
        String fileId = file.getId();
        assertEquals("fine-tune", file.getPurpose());
        assertEquals(filePath, file.getFilename());
        // wait for file to be processed
        TimeUnit.SECONDS.sleep(10);

        List<File> files = service.listFiles();
        assertTrue(files.stream().anyMatch(fileItem -> fileItem.getId().equals(fileId)));
        file = service.retrieveFile(fileId);
        assertEquals(filePath, file.getFilename());
        String fileBytesToString = service.retrieveFileContent(fileId).string();
        String contents = new String(Files.readAllBytes(new java.io.File(getClass().getClassLoader().getResource(filePath).getFile()).toPath()), StandardCharsets.UTF_8);
        assertEquals(contents, fileBytesToString);
        DeleteResult result = service.deleteFile(fileId);
        assertTrue(result.isDeleted());
    }


}
