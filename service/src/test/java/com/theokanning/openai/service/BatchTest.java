package com.theokanning.openai.service;

import com.theokanning.openai.ListSearchParameters;
import com.theokanning.openai.batch.Batch;
import com.theokanning.openai.batch.BatchRequest;
import org.junit.jupiter.api.*;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author LiangTao
 * @date 2024年04月26 15:17
 **/
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BatchTest {

    static OpenAiService service = new OpenAiService();

    static String fileId;

    static String batchId;

    @BeforeAll
    static void setup() throws Exception {
        fileId = service.uploadFile("batch", "src/test/resources/batch-task-data.jsonl").getId();
        // wait for file to be processed
        TimeUnit.SECONDS.sleep(10);
    }

    @AfterAll
    static void teardown() {
        try {
            service.deleteFile(fileId);
        } catch (Exception e) {
            // ignore
        }
    }

    @Test
    @Order(1)
    void createBatchTask() {
        Batch batch = service.createBatch(BatchRequest.builder()
                .inputFileId(fileId)
                .completionWindow("24h")
                .endpoint("/v1/chat/completions")
                .build()
        );
        batchId = batch.getId();
        assertNotNull(batch);
    }

    @Test
    @Order(2)
    void listBatchTasks() {
        assertNotNull(service.listBatches(ListSearchParameters.builder().build()));
    }

    @Test
    @Order(3)
    void retrieveBatchTask() {
        assertNotNull(service.retrieveBatch(batchId));
    }

    @Test
    @Order(3)
    void cancelBatchTask() {
        Batch canelBatch = service.cancelBatch(batchId);
        assertNotNull(canelBatch);
        assertEquals("cancelling", canelBatch.getStatus());
    }


}
