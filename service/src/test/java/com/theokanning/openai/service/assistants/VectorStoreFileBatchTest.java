package com.theokanning.openai.service.assistants;

import com.theokanning.openai.DeleteResult;
import com.theokanning.openai.ListSearchParameters;
import com.theokanning.openai.assistants.vector_store.VectorStore;
import com.theokanning.openai.assistants.vector_store.VectorStoreRequest;
import com.theokanning.openai.assistants.vector_store_file.VectorStoreFile;
import com.theokanning.openai.assistants.vector_store_file_batch.VectorStoreFilesBatch;
import com.theokanning.openai.assistants.vector_store_file_batch.VectorStoreFilesBatchRequest;
import com.theokanning.openai.service.OpenAiService;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author LiangTao
 * @date 2024年04月28 21:45
 **/
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VectorStoreFileBatchTest {

    static OpenAiService service = new OpenAiService();

    static String vectorStoreId;

    static List<String> fileIds = new ArrayList<>();

    @BeforeAll
    static void uploadFile() {
        //上传文件
        fileIds.add(service.uploadFile("assistants", "src/test/resources/田山歌中艺术特征及其共生性特征探析.txt").getId());
        fileIds.add(service.uploadFile("assistants", "src/test/resources/useexapmle.txt").getId());

        VectorStoreRequest request = VectorStoreRequest.builder()
                .name("vector_store_file_batch_test")
                .build();
        VectorStore vectorStore = service.createVectorStore(request);
        assertNotNull(vectorStore);
        vectorStoreId = vectorStore.getId();
    }

    @AfterAll
    static void deleteTestData() {
        DeleteResult deleteResult = service.deleteVectorStore(vectorStoreId);
        for (String fileId : fileIds) {
            service.deleteFile(fileId);
        }
    }

    @Test
    void vectorStoreFileBatchTest() {
        VectorStoreFilesBatch vectorStoreFileBatch = service.createVectorStoreFileBatch(vectorStoreId, VectorStoreFilesBatchRequest.builder()
                .fileIds(fileIds)
                .build()
        );
        assertEquals(2, vectorStoreFileBatch.getFileCounts().getTotal());
        assertEquals(vectorStoreId, vectorStoreFileBatch.getVectorStoreId());

        service.cancelVectorStoreFileBatch(vectorStoreId, vectorStoreFileBatch.getId());
        VectorStoreFilesBatch vectorStoreFilesBatch = service.retrieveVectorStoreFileBatch(vectorStoreId, vectorStoreFileBatch.getId());
        assertEquals(vectorStoreFileBatch.getId(), vectorStoreFilesBatch.getId());


        List<VectorStoreFile> vectorStoreFile = service.listVectorStoreFilesInBatch(vectorStoreId, vectorStoreFileBatch.getId(), new ListSearchParameters()).getData();
        assertTrue(vectorStoreFile.stream().allMatch(item -> fileIds.contains(item.getId())));
    }


}
