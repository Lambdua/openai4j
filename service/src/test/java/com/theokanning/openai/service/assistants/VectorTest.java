package com.theokanning.openai.service.assistants;

import com.theokanning.openai.DeleteResult;
import com.theokanning.openai.ListSearchParameters;
import com.theokanning.openai.assistants.vector_store.ModifyVectorStoreRequest;
import com.theokanning.openai.assistants.vector_store.VectorStore;
import com.theokanning.openai.assistants.vector_store.VectorStoreRequest;
import com.theokanning.openai.service.OpenAiService;
import org.junit.jupiter.api.*;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 向量存储测试类
 *
 * @author LiangTao
 * @date 2024年04月28 17:24
 **/
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VectorTest {
    static OpenAiService service = new OpenAiService();

    static String vectorStoreId;

    static String fileId;

    @BeforeAll
    static void uploadFile() {
        //上传文件
        fileId = service.uploadFile("assistants", "src/test/resources/田山歌中艺术特征及其共生性特征探析.txt").getId();
    }

    @AfterAll
    static void deleteTestData() {
        service.deleteFile(fileId);
    }


    @Order(1)
    @Test
    void createVectorStore() {
        VectorStoreRequest request = VectorStoreRequest.builder()
                .name("test_vector_store")
                .fileIds(Collections.singletonList(fileId))
                .build();
        VectorStore vectorStore = service.createVectorStore(request);
        assertNotNull(vectorStore);
        vectorStoreId = vectorStore.getId();
    }

    @Order(2)
    @Test
    void listVectorStores() {
        List<VectorStore> vectorStores = service.listVectorStores(new ListSearchParameters()).getData();
        assertNotNull(vectorStores);
        assertFalse(vectorStores.isEmpty());
        assertTrue(vectorStores.stream().anyMatch(item -> item.getId().equals(vectorStoreId)));
    }

    @Test
    @Order(3)
    void retrieveVectorStore() {
        VectorStore vectorStore = service.retrieveVectorStore(vectorStoreId);
        assertNotNull(vectorStore);
        assertEquals(vectorStoreId, vectorStore.getId());
    }

    @Test
    @Order(4)
    void modifyVectorStore() {
        VectorStore vectorStore = service.modifyVectorStore(vectorStoreId, ModifyVectorStoreRequest.builder()
                .name("test_vector_store_modify").build()
        );
        assertNotNull(vectorStore);
        assertEquals("test_vector_store_modify", vectorStore.getName());
    }

    @Test
    @Order(5)
    void deleteVectorStore() {
        DeleteResult deleteResult = service.deleteVectorStore(vectorStoreId);
        assertTrue(deleteResult.isDeleted());
        List<VectorStore> vectorStores = service.listVectorStores(new ListSearchParameters()).getData();
        assertFalse(vectorStores.stream().anyMatch(vectorStore -> vectorStore.getId().equals(vectorStoreId)));
    }

}
