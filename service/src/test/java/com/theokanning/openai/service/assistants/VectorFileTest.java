package com.theokanning.openai.service.assistants;

import com.theokanning.openai.DeleteResult;
import com.theokanning.openai.ListSearchParameters;
import com.theokanning.openai.assistants.assistant.VectorStoreFileRequest;
import com.theokanning.openai.assistants.vector_store.VectorStore;
import com.theokanning.openai.assistants.vector_store.VectorStoreRequest;
import com.theokanning.openai.assistants.vector_store_file.VectorStoreFile;
import com.theokanning.openai.service.OpenAiService;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author LiangTao
 * @date 2024年04月28 18:17
 **/
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VectorFileTest {
    static OpenAiService service = new OpenAiService();

    static String vectorStoreId;

    static List<String> fileIds = new ArrayList<>();


    @BeforeAll
    static void uploadFile() {
        //上传文件
        fileIds.add(service.uploadFile("assistants", "src/test/resources/田山歌中艺术特征及其共生性特征探析.txt").getId());
        VectorStoreRequest request = VectorStoreRequest.builder()
                .name("vector_store_file_test")
                .fileIds(fileIds)
                .build();
        VectorStore vectorStore = service.createVectorStore(request);
        assertNotNull(vectorStore);
        vectorStoreId = vectorStore.getId();
    }

    @AfterAll
    static void deleteTestData() {
        service.deleteVectorStore(vectorStoreId);
        for (String fileId : fileIds) {
            service.deleteFile(fileId);
        }
    }

    @Test
    @Order(1)
    void createVectorStoreFileTest() {
        //上传新文件
        fileIds.add(service.uploadFile("assistants", "src/test/resources/useexapmle.txt").getId());
        VectorStoreFile vectorStoreFile = service.createVectorStoreFile(vectorStoreId, VectorStoreFileRequest.builder()
                .fileId(fileIds.get(1))
                .build()
        );
        assertEquals(vectorStoreId, vectorStoreFile.getVectorStoreId());
    }

    @Test
    @Order(2)
    void listVectorStoreFilesTest() throws InterruptedException {
        //等待文件上传完成
        Thread.sleep(5000);
        List<VectorStoreFile> vectorStoreFiles = service.listVectorStoreFiles(vectorStoreId, new ListSearchParameters()).getData();
        assertNotNull(vectorStoreFiles);
        assertTrue(vectorStoreFiles.stream().anyMatch(i -> i.getVectorStoreId().equals(vectorStoreId)));
    }

    @Test
    @Order(3)
    void retrieveAndDeleteVectorStoreFileTest() {
        VectorStoreFile vectorStoreFile = service.retrieveVectorStoreFile(vectorStoreId, fileIds.get(1));
        assertNotNull(vectorStoreFile);
        DeleteResult deleteResult = service.deleteVectorStoreFile(vectorStoreId, vectorStoreFile.getId());
        assertTrue(deleteResult.isDeleted());
        List<VectorStoreFile> vsf = service.listVectorStoreFiles(vectorStoreId, new ListSearchParameters()).getData();
        assertTrue(vsf.stream().noneMatch(f -> f.getId().equals(vectorStoreFile.getId())));
    }
}
