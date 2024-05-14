package com.theokanning.openai.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.theokanning.openai.*;
import com.theokanning.openai.assistants.assistant.Assistant;
import com.theokanning.openai.assistants.assistant.AssistantRequest;
import com.theokanning.openai.assistants.assistant.ModifyAssistantRequest;
import com.theokanning.openai.assistants.assistant.VectorStoreFileRequest;
import com.theokanning.openai.assistants.message.Message;
import com.theokanning.openai.assistants.message.MessageListSearchParameters;
import com.theokanning.openai.assistants.message.MessageRequest;
import com.theokanning.openai.assistants.message.ModifyMessageRequest;
import com.theokanning.openai.assistants.run.*;
import com.theokanning.openai.assistants.run_step.RunStep;
import com.theokanning.openai.assistants.thread.Thread;
import com.theokanning.openai.assistants.thread.ThreadRequest;
import com.theokanning.openai.assistants.vector_store.ModifyVectorStoreRequest;
import com.theokanning.openai.assistants.vector_store.VectorStore;
import com.theokanning.openai.assistants.vector_store.VectorStoreRequest;
import com.theokanning.openai.assistants.vector_store_file.VectorStoreFile;
import com.theokanning.openai.assistants.vector_store_file_batch.VectorStoreFilesBatch;
import com.theokanning.openai.assistants.vector_store_file_batch.VectorStoreFilesBatchRequest;
import com.theokanning.openai.audio.*;
import com.theokanning.openai.batch.Batch;
import com.theokanning.openai.batch.BatchRequest;
import com.theokanning.openai.billing.BillingUsage;
import com.theokanning.openai.billing.Subscription;
import com.theokanning.openai.client.AuthenticationInterceptor;
import com.theokanning.openai.client.OpenAiApi;
import com.theokanning.openai.completion.CompletionChunk;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.CompletionResult;
import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.edit.EditRequest;
import com.theokanning.openai.edit.EditResult;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResult;
import com.theokanning.openai.file.File;
import com.theokanning.openai.fine_tuning.FineTuningEvent;
import com.theokanning.openai.fine_tuning.FineTuningJob;
import com.theokanning.openai.fine_tuning.FineTuningJobCheckpoint;
import com.theokanning.openai.fine_tuning.FineTuningJobRequest;
import com.theokanning.openai.image.CreateImageEditRequest;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.CreateImageVariationRequest;
import com.theokanning.openai.image.ImageResult;
import com.theokanning.openai.model.Model;
import com.theokanning.openai.moderation.ModerationRequest;
import com.theokanning.openai.moderation.ModerationResult;
import com.theokanning.openai.service.assistant_stream.AssistantResponseBodyCallback;
import com.theokanning.openai.service.assistant_stream.AssistantSSE;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import okhttp3.*;
import retrofit2.Call;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.validation.constraints.NotNull;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class OpenAiService {

    private static final String DEFAULT_BASE_URL = "https://api.openai.com/v1/";

    public static final String API_BASE_URL_ENV = "OPENAI_API_BASE_URL";

    public static final String API_KEY_ENV = "OPENAI_API_KEY";

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final ObjectMapper mapper = defaultObjectMapper();

    private final OpenAiApi api;
    private final ExecutorService executorService;

    /**
     * Creates a new OpenAiService that wraps OpenAiApi,user OPENAI_API_KEY from environment variable
     */
    public OpenAiService() {
        this(System.getenv(API_KEY_ENV));
    }

    public OpenAiService(Duration timeout) {
        this(System.getenv(API_KEY_ENV), timeout);
    }

    /**
     * Creates a new OpenAiService that wraps OpenAiApi
     *
     * @param token OpenAi token string "sk-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
     */
    public OpenAiService(final String token) {
        this(token, DEFAULT_TIMEOUT, System.getenv(API_BASE_URL_ENV) != null ? System.getenv(API_BASE_URL_ENV) : DEFAULT_BASE_URL);
    }

    public OpenAiService(final String token, final String baseUrl) {
        this(token, DEFAULT_TIMEOUT, baseUrl);
    }

    /**
     * Creates a new OpenAiService that wraps OpenAiApi
     *
     * @param token   OpenAi token string "sk-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
     * @param timeout http read timeout, Duration.ZERO means no timeout
     */
    public OpenAiService(final String token, final Duration timeout) {
        this(token, timeout, System.getenv(API_BASE_URL_ENV) != null ? System.getenv(API_BASE_URL_ENV) : DEFAULT_BASE_URL);
    }

    /**
     * Creates a new OpenAiService that wraps OpenAiApi
     *
     * @param token   OpenAi token string "sk-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
     * @param timeout http read timeout, Duration.ZERO means no timeout
     * @param baseUrl OpenAi API base URL, default is "https://api.openai.com/v1/"
     */
    public OpenAiService(final String token, final Duration timeout, String baseUrl) {
        ObjectMapper mapper = defaultObjectMapper();
        OkHttpClient client = defaultClient(token, timeout);
        Retrofit retrofit = defaultRetrofit(client, mapper, baseUrl);

        this.api = retrofit.create(OpenAiApi.class);
        this.executorService = client.dispatcher().executorService();
    }

    /**
     * Creates a new OpenAiService that wraps OpenAiApi.
     * Use this if you need more customization, but use OpenAiService(api, executorService) if you use streaming and
     * want to shut down instantly
     *
     * @param api OpenAiApi instance to use for all methods
     */
    public OpenAiService(final OpenAiApi api) {
        this.api = api;
        this.executorService = null;
    }

    /**
     * Creates a new OpenAiService that wraps OpenAiApi.
     * The ExecutorService must be the one you get from the client you created the api with
     * otherwise shutdownExecutor() won't work.
     * <p>
     * Use this if you need more customization.
     *
     * @param api             OpenAiApi instance to use for all methods
     * @param executorService the ExecutorService from client.dispatcher().executorService()
     */
    public OpenAiService(final OpenAiApi api, final ExecutorService executorService) {
        this.api = api;
        this.executorService = executorService;
    }

    public List<Model> listModels() {
        return execute(api.listModels()).data;
    }

    public Model getModel(String modelId) {
        return execute(api.getModel(modelId));
    }

    public static OpenAiApi buildApi(String token, Duration timeout) {
        return buildApi(token, timeout, System.getenv(API_BASE_URL_ENV) != null ? System.getenv(API_BASE_URL_ENV) : DEFAULT_BASE_URL);
    }

    public static ObjectMapper defaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.addMixIn(ChatFunction.class, ChatFunctionMixIn.class);
        return mapper;
    }

    public ChatCompletionResult createChatCompletion(ChatCompletionRequest request) {
        return execute(api.createChatCompletion(request));
    }

    public Flowable<ChatCompletionChunk> streamChatCompletion(ChatCompletionRequest request) {
        request.setStream(true);
        return stream(api.createChatCompletionStream(request), ChatCompletionChunk.class);
    }

    public EditResult createEdit(EditRequest request) {
        return execute(api.createEdit(request));
    }

    public EmbeddingResult createEmbeddings(EmbeddingRequest request) {
        return execute(api.createEmbeddings(request));
    }

    public List<File> listFiles() {
        return execute(api.listFiles()).data;
    }

    @Deprecated
    public CompletionResult createCompletion(CompletionRequest request) {
        return execute(api.createCompletion(request));
    }

    public DeleteResult deleteFile(String fileId) {
        return execute(api.deleteFile(fileId));
    }

    public File retrieveFile(String fileId) {
        return execute(api.retrieveFile(fileId));
    }

    public ResponseBody retrieveFileContent(String fileId) {
        return execute(api.retrieveFileContent(fileId));
    }

    public FineTuningJob createFineTuningJob(FineTuningJobRequest request) {
        return execute(api.createFineTuningJob(request));
    }

    public List<FineTuningJob> listFineTuningJobs() {
        return execute(api.listFineTuningJobs()).data;
    }

    public FineTuningJob retrieveFineTuningJob(String fineTuningJobId) {
        return execute(api.retrieveFineTuningJob(fineTuningJobId));
    }

    public FineTuningJob cancelFineTuningJob(String fineTuningJobId) {
        return execute(api.cancelFineTuningJob(fineTuningJobId));
    }

    public List<FineTuningEvent> listFineTuningJobEvents(String fineTuningJobId) {
        return execute(api.listFineTuningJobEvents(fineTuningJobId)).data;
    }

    public List<FineTuningJobCheckpoint> listFineTuningCheckpoints(String fineTuningJobId) {
        return execute(api.listFineTuningCheckpoints(fineTuningJobId)).data;
    }

    @Deprecated
    public Flowable<CompletionChunk> streamCompletion(CompletionRequest request) {
        request.setStream(true);
        return stream(api.createCompletionStream(request), CompletionChunk.class);
    }

    /**
     * @param purpose file purpose,support: batch,fine-tune,assistants
     */
    public File uploadFile(String purpose, String filepath) {
        java.io.File file = new java.io.File(filepath);
        try {
            return uploadFile(purpose, new FileInputStream(file), file.getName());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Upload a file using InputStream.
     *
     * @param purpose         file purpose, Use "assistants" for Assistants and Messages, "batch" for Batch API, and "fine-tune" for Fine-tuning.
     * @param fileInputStream the input stream of the file to be uploaded
     * @param filename        the name of the file to be uploaded
     * @return the File object returned by the API after the file is uploaded
     */
    public File uploadFile(String purpose, InputStream fileInputStream, String filename) {
        RequestBody purposeBody = RequestBody.create(MultipartBody.FORM, purpose);
        RequestBody fileBody = RequestBody.create(FileUtil.getFileUploadMediaType(filename), FileUtil.readAllBytes(fileInputStream));
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", filename, fileBody);
        return execute(api.uploadFile(purposeBody, body));
    }


    public Batch createBatch(BatchRequest request) {
        return execute(api.createBatch(request));
    }

    public Batch retrieveBatch(String batchId) {
        return execute(api.retrieveBatch(batchId));
    }

    public static Flowable<AssistantSSE> assistantStream(Call<ResponseBody> apiCall) {
        return Flowable.create(emitter -> apiCall.enqueue(new AssistantResponseBodyCallback(emitter)), BackpressureStrategy.BUFFER);
    }


    public ImageResult createImage(CreateImageRequest request) {
        return execute(api.createImage(request));
    }

    public ImageResult createImageEdit(CreateImageEditRequest request, String imagePath, String maskPath) {
        java.io.File image = new java.io.File(imagePath);
        java.io.File mask = null;
        if (maskPath != null) {
            mask = new java.io.File(maskPath);
        }
        return createImageEdit(request, image, mask);
    }

    public ImageResult createImageEdit(CreateImageEditRequest request, java.io.File image, java.io.File mask) {
        RequestBody imageBody = RequestBody.create(MediaType.parse("image"), image);

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MediaType.get("multipart/form-data"))
                .addFormDataPart("prompt", request.getPrompt())
                .addFormDataPart("size", request.getSize())
                .addFormDataPart("response_format", request.getResponseFormat())
                .addFormDataPart("image", "image", imageBody);

        if (request.getN() != null) {
            builder.addFormDataPart("n", request.getN().toString());
        }

        if (mask != null) {
            RequestBody maskBody = RequestBody.create(MediaType.parse("image"), mask);
            builder.addFormDataPart("mask", "mask", maskBody);
        }

        if (request.getModel() != null) {
            builder.addFormDataPart("model", request.getModel());
        }

        return execute(api.createImageEdit(builder.build()));
    }

    public ImageResult createImageVariation(CreateImageVariationRequest request, String imagePath) {
        java.io.File image = new java.io.File(imagePath);
        return createImageVariation(request, image);
    }

    public ImageResult createImageVariation(CreateImageVariationRequest request, java.io.File image) {
        RequestBody imageBody = RequestBody.create(MediaType.parse("image"), image);

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MediaType.get("multipart/form-data"))
                .addFormDataPart("size", request.getSize())
                .addFormDataPart("response_format", request.getResponseFormat())
                .addFormDataPart("image", "image", imageBody);

        if (request.getN() != null) {
            builder.addFormDataPart("n", request.getN().toString());
        }

        if (request.getModel() != null) {
            builder.addFormDataPart("model", request.getModel());
        }

        return execute(api.createImageVariation(builder.build()));
    }

    public TranscriptionResult createTranscription(CreateTranscriptionRequest request, String audioPath) {
        java.io.File audio = new java.io.File(audioPath);
        return createTranscription(request, audio);
    }

    public TranscriptionResult createTranscription(CreateTranscriptionRequest request, java.io.File audio) {
        RequestBody audioBody = RequestBody.create(MediaType.parse("audio"), audio);

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MediaType.get("multipart/form-data"))
                .addFormDataPart("model", request.getModel())
                .addFormDataPart("file", audio.getName(), audioBody);

        if (request.getPrompt() != null) {
            builder.addFormDataPart("prompt", request.getPrompt());
        }
        if (request.getResponseFormat() != null) {
            builder.addFormDataPart("response_format", request.getResponseFormat());
        }
        if (request.getTemperature() != null) {
            builder.addFormDataPart("temperature", request.getTemperature().toString());
        }
        if (request.getLanguage() != null) {
            builder.addFormDataPart("language", request.getLanguage());
        }
        if (request.getTimestampGranularities() != null && !request.getTimestampGranularities().isEmpty()) {
            for (String granularity : request.getTimestampGranularities()) {
                builder.addFormDataPart("timestamp_granularities[]", granularity);
            }
        }
        return execute(api.createTranscription(builder.build()));
    }

    public TranslationResult createTranslation(CreateTranslationRequest request, String audioPath) {
        java.io.File audio = new java.io.File(audioPath);
        return createTranslation(request, audio);
    }

    public TranslationResult createTranslation(CreateTranslationRequest request, java.io.File audio) {
        RequestBody audioBody = RequestBody.create(MediaType.parse("audio"), audio);

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MediaType.get("multipart/form-data"))
                .addFormDataPart("model", request.getModel())
                .addFormDataPart("file", audio.getName(), audioBody);

        if (request.getPrompt() != null) {
            builder.addFormDataPart("prompt", request.getPrompt());
        }
        if (request.getResponseFormat() != null) {
            builder.addFormDataPart("response_format", request.getResponseFormat());
        }
        if (request.getTemperature() != null) {
            builder.addFormDataPart("temperature", request.getTemperature().toString());
        }

        return execute(api.createTranslation(builder.build()));
    }

    public ModerationResult createModeration(ModerationRequest request) {
        return execute(api.createModeration(request));
    }

    public ResponseBody createSpeech(CreateSpeechRequest request) {
        return execute(api.createSpeech(request));
    }

    public Assistant createAssistant(AssistantRequest request) {
        return execute(api.createAssistant(request));
    }

    public Assistant retrieveAssistant(String assistantId) {
        return execute(api.retrieveAssistant(assistantId));
    }

    public Assistant modifyAssistant(String assistantId, ModifyAssistantRequest request) {
        return execute(api.modifyAssistant(assistantId, request));
    }

    public DeleteResult deleteAssistant(String assistantId) {
        return execute(api.deleteAssistant(assistantId));
    }

    public OpenAiResponse<Assistant> listAssistants(ListSearchParameters params) {
        Map<String, Object> queryParameters = mapper.convertValue(params, new TypeReference<Map<String, Object>>() {
        });
        return execute(api.listAssistants(queryParameters));
    }

    public Thread createThread(ThreadRequest request) {
        return execute(api.createThread(request));
    }

    public Thread retrieveThread(String threadId) {
        return execute(api.retrieveThread(threadId));
    }

    public Thread modifyThread(String threadId, ThreadRequest request) {
        return execute(api.modifyThread(threadId, request));
    }

    public DeleteResult deleteThread(String threadId) {
        return execute(api.deleteThread(threadId));
    }

    public Message createMessage(String threadId, MessageRequest request) {
        return execute(api.createMessage(threadId, request));
    }

    public Message retrieveMessage(String threadId, String messageId) {
        return execute(api.retrieveMessage(threadId, messageId));
    }

    public Message modifyMessage(String threadId, String messageId, ModifyMessageRequest request) {
        return execute(api.modifyMessage(threadId, messageId, request));
    }

    public OpenAiResponse<Message> listMessages(String threadId, MessageListSearchParameters params) {
        Map<String, Object> queryParameters = mapper.convertValue(params, new TypeReference<Map<String, Object>>() {
        });
        return execute(api.listMessages(threadId, queryParameters));
    }

    public DeleteResult deleteMessage(String threadId, String messageId) {
        return execute(api.deleteMessage(threadId, messageId));
    }


    public Run createRun(String threadId, RunCreateRequest runCreateRequest) {
        return execute(api.createRun(threadId, runCreateRequest));
    }

    public OpenAiResponse<Batch> listBatches(ListSearchParameters params) {
        Map<String, Object> queryParameters = mapper.convertValue(params, new TypeReference<Map<String, Object>>() {
        });
        return execute(api.listBatches(queryParameters));
    }


    public Run retrieveRun(String threadId, String runId) {
        return execute(api.retrieveRun(threadId, runId));
    }

    public Run modifyRun(String threadId, String runId, ModifyRunRequest request) {
        return execute(api.modifyRun(threadId, runId, request));
    }

    public OpenAiResponse<Run> listRuns(String threadId, ListSearchParameters listSearchParameters) {
        Map<String, String> search = new HashMap<>();
        if (listSearchParameters != null) {
            search = mapper.convertValue(listSearchParameters, Map.class);
        }
        return execute(api.listRuns(threadId, search));
    }

    public Run submitToolOutputs(String threadId, String runId, SubmitToolOutputsRequest submitToolOutputsRequest) {
        return execute(api.submitToolOutputs(threadId, runId, submitToolOutputsRequest));
    }

    public Flowable<AssistantSSE> createRunStream(String threadId, RunCreateRequest runCreateRequest) {
        runCreateRequest.setStream(true);
        return assistantStream(api.createRunStream(threadId, runCreateRequest));
    }


    public Run cancelRun(String threadId, String runId) {
        return execute(api.cancelRun(threadId, runId));
    }

    public Run createThreadAndRun(CreateThreadAndRunRequest createThreadAndRunRequest) {
        return execute(api.createThreadAndRun(createThreadAndRunRequest));
    }

    public Flowable<AssistantSSE> createThreadAndRunStream(CreateThreadAndRunRequest createThreadAndRunRequest) {
        createThreadAndRunRequest.setStream(true);
        return assistantStream(api.createThreadAndRunStream(createThreadAndRunRequest));
    }


    public RunStep retrieveRunStep(String threadId, String runId, String stepId) {
        return execute(api.retrieveRunStep(threadId, runId, stepId));
    }

    public OpenAiResponse<RunStep> listRunSteps(String threadId, String runId, ListSearchParameters listSearchParameters) {
        Map<String, String> search = new HashMap<>();
        if (listSearchParameters != null) {
            search = mapper.convertValue(listSearchParameters, Map.class);
        }
        return execute(api.listRunSteps(threadId, runId, search));
    }


    public VectorStore createVectorStore(VectorStoreRequest request) {
        return execute(api.createVectorStore(request));
    }

    public OpenAiResponse<VectorStore> listVectorStores(ListSearchParameters listSearchParameters) {
        Map<String, Object> search = new HashMap<>();
        if (listSearchParameters != null) {
            search = mapper.convertValue(listSearchParameters, Map.class);
        }
        return execute(api.listVectorStores(search));
    }

    public VectorStore retrieveVectorStore(String vectorStoreId) {
        return execute(api.retrieveVectorStore(vectorStoreId));
    }

    public VectorStore modifyVectorStore(String vectorStoreId, ModifyVectorStoreRequest request) {
        return execute(api.modifyVectorStore(vectorStoreId, request));
    }

    public DeleteResult deleteVectorStore(String vectorStoreId) {
        return execute(api.deleteVectorStore(vectorStoreId));
    }

    public VectorStoreFile createVectorStoreFile(String vectorStoreId, VectorStoreFileRequest fileRequest) {
        return execute(api.createVectorStoreFile(vectorStoreId, fileRequest));
    }

    public OpenAiResponse<VectorStoreFile> listVectorStoreFiles(String vectorStoreId, ListSearchParameters listSearchParameters) {
        Map<String, Object> search = new HashMap<>();
        if (listSearchParameters != null) {
            search = mapper.convertValue(listSearchParameters, Map.class);
        }
        return execute(api.listVectorStoreFiles(vectorStoreId, search));
    }

    public VectorStoreFile retrieveVectorStoreFile(String vectorStoreId, String fileId) {
        return execute(api.retrieveVectorStoreFile(vectorStoreId, fileId));
    }

    public DeleteResult deleteVectorStoreFile(String vectorStoreId, String fileId) {
        return execute(api.deleteVectorStoreFile(vectorStoreId, fileId));
    }

    public VectorStoreFilesBatch createVectorStoreFileBatch(String vectorStoreId, VectorStoreFilesBatchRequest request) {
        return execute(api.createVectorStoreFileBatch(vectorStoreId, request));
    }

    public VectorStoreFilesBatch retrieveVectorStoreFileBatch(String vectorStoreId, String batchId) {
        return execute(api.retrieveVectorStoreFileBatch(vectorStoreId, batchId));
    }

    public VectorStoreFilesBatch cancelVectorStoreFileBatch(String vectorStoreId, String batchId) {
        return execute(api.cancelVectorStoreFileBatch(vectorStoreId, batchId));
    }

    public OpenAiResponse<VectorStoreFile> listVectorStoreFilesInBatch(String vectorStoreId, String batchId, ListSearchParameters listSearchParameters) {
        Map<String, Object> search = new HashMap<>();
        if (listSearchParameters != null) {
            search = mapper.convertValue(listSearchParameters, Map.class);
        }
        return execute(api.listVectorStoreFilesInBatch(vectorStoreId, batchId, search));
    }

    public Flowable<AssistantSSE> submitToolOutputsStream(String threadId, String runId, SubmitToolOutputsRequest submitToolOutputsRequest) {
        submitToolOutputsRequest.setStream(true);
        return assistantStream(api.submitToolOutputsStream(threadId, runId, submitToolOutputsRequest));
    }

    /**
     * Account information inquiry: including total amount and other information.
     *
     * @return Account information.
     */
    public Subscription subscription() {
        Single<Subscription> subscription = api.subscription();
        return subscription.blockingGet();
    }

    /**
     * Calls the Open AI api, returns the response, and parses error messages if the request fails
     */
    public static <T> T execute(Single<T> apiCall) {
        try {
            return apiCall.blockingGet();
        } catch (HttpException e) {
            try {
                if (e.response() == null || e.response().errorBody() == null) {
                    throw e;
                }
                String errorBody = e.response().errorBody().string();

                OpenAiError error = mapper.readValue(errorBody, OpenAiError.class);
                throw new OpenAiHttpException(error, e, e.code());
            } catch (IOException ex) {
                // couldn't parse OpenAI error
                throw e;
            }
        }
    }

    /**
     * Calls the Open AI api and returns a Flowable of SSE for streaming
     * omitting the last message.
     *
     * @param apiCall The api call
     */
    public static Flowable<SSE> stream(Call<ResponseBody> apiCall) {
        return stream(apiCall, false);
    }

    /**
     * Account API consumption amount information inquiry.
     * Up to 100 days of inquiry.
     *
     * @param starDate
     * @param endDate
     * @return Consumption amount information.
     */
    public BillingUsage billingUsage(@NotNull LocalDate starDate, @NotNull LocalDate endDate) {
        Single<BillingUsage> billingUsage = api.billingUsage(starDate, endDate);
        return billingUsage.blockingGet();
    }


    /**
     * Calls the Open AI api and returns a Flowable of SSE for streaming.
     *
     * @param apiCall  The api call
     * @param emitDone If true the last message ([DONE]) is emitted
     */
    public static Flowable<SSE> stream(Call<ResponseBody> apiCall, boolean emitDone) {
        return Flowable.create(emitter -> apiCall.enqueue(new ResponseBodyCallback(emitter, emitDone)), BackpressureStrategy.BUFFER);
    }

    /**
     * Calls the Open AI api and returns a Flowable of type T for streaming
     * omitting the last message.
     *
     * @param apiCall The api call
     * @param cl      Class of type T to return
     */
    public static <T> Flowable<T> stream(Call<ResponseBody> apiCall, Class<T> cl) {
        return stream(apiCall).map(sse -> mapper.readValue(sse.getData(), cl));
    }

    /**
     * Shuts down the OkHttp ExecutorService.
     * The default behaviour of OkHttp's ExecutorService (ConnectionPool)
     * is to shut down after an idle timeout of 60s.
     * Call this method to shut down the ExecutorService immediately.
     */
    public void shutdownExecutor() {
        Objects.requireNonNull(this.executorService, "executorService must be set in order to shut down");
        this.executorService.shutdown();
    }

    public Batch cancelBatch(String batchId) {
        return execute(api.cancelBatch(batchId));
    }

    public static OpenAiApi buildApi(String token, Duration timeout, String baseUrl) {
        OkHttpClient client = defaultClient(token, timeout);
        Retrofit retrofit = defaultRetrofit(client, mapper, baseUrl);

        return retrofit.create(OpenAiApi.class);
    }


    public static OkHttpClient defaultClient(String token, Duration timeout) {
        return new OkHttpClient.Builder()
                .addInterceptor(new AuthenticationInterceptor(token))
                .connectionPool(new ConnectionPool(5, 1, TimeUnit.SECONDS))
                .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .build();
    }

    public static Retrofit defaultRetrofit(OkHttpClient client, ObjectMapper mapper, String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    public Flowable<ChatMessageAccumulator> mapStreamToAccumulator(Flowable<ChatCompletionChunk> flowable) {
        ChatFunctionCall functionCall = new ChatFunctionCall(null, null);
        AssistantMessage accumulatedMessage = new AssistantMessage();

        return flowable.map(chunk -> {
            ChatCompletionChoice firstChoice = chunk.getChoices().get(0);
            AssistantMessage messageChunk = firstChoice.getMessage();
            if (messageChunk.getFunctionCall() != null) {
                if (messageChunk.getFunctionCall().getName() != null) {
                    String namePart = messageChunk.getFunctionCall().getName();
                    functionCall.setName((functionCall.getName() == null ? "" : functionCall.getName()) + namePart);
                }
                if (messageChunk.getFunctionCall().getArguments() != null) {
                    String argumentsPart = messageChunk.getFunctionCall().getArguments() == null ? "" : messageChunk.getFunctionCall().getArguments().asText();
                    functionCall.setArguments(new TextNode((functionCall.getArguments() == null ? "" : functionCall.getArguments().asText()) + argumentsPart));
                }
                accumulatedMessage.setFunctionCall(functionCall);
            } else if (messageChunk.getToolCalls() != null) {
                List<ChatToolCall> toolCalls = messageChunk.getToolCalls();
                ChatToolCall partToolCall = toolCalls.get(0);
                ChatFunctionCall partFunction = partToolCall.getFunction();
                int index = partToolCall.getIndex();
                List<ChatToolCall> accumulatedChatTools = accumulatedMessage.getToolCalls();
                if (accumulatedChatTools == null) {
                    accumulatedChatTools = new ArrayList<>();
                    accumulatedMessage.setToolCalls(accumulatedChatTools);
                }
                ChatToolCall accumelatedToolCall = accumulatedChatTools.stream().filter(chatToolCall -> chatToolCall.getIndex() == index).findFirst().orElse(null);
                if (accumelatedToolCall == null) {
                    accumelatedToolCall = new ChatToolCall(index, partToolCall.getId(), partToolCall.getType());
                    accumulatedChatTools.add(accumelatedToolCall);
                }
                ChatFunctionCall function = accumelatedToolCall.getFunction();
                if (partFunction.getName() != null) {
                    function.setName((function.getName() == null ? "" : function.getName()) + partFunction.getName());
                }
                if (partFunction.getArguments() != null) {
                    function.setArguments(new TextNode((function.getArguments() == null ? "" : function.getArguments().asText()) + partFunction.getArguments().asText()));
                }
                accumelatedToolCall.setFunction(function);
            } else {
                accumulatedMessage.setContent((accumulatedMessage.getContent() == null ? "" : accumulatedMessage.getContent()) + (messageChunk.getContent() == null ? "" : messageChunk.getContent()));
            }

            if (firstChoice.getFinishReason() != null) { // last
                if ("function_call".equals(firstChoice.getFinishReason()) && functionCall.getArguments() != null) {
                    functionCall.setArguments(mapper.readTree(functionCall.getArguments().asText()));
                    accumulatedMessage.setFunctionCall(functionCall);
                }
                if ("tool_calls".equals(firstChoice.getFinishReason()) && accumulatedMessage.getToolCalls() != null) {
                    //按照index重新排序
                    accumulatedMessage.getToolCalls().sort(Comparator.comparingInt(ChatToolCall::getIndex));
                    for (ChatToolCall chatToolCall : accumulatedMessage.getToolCalls()) {
                        if (chatToolCall.getFunction().getArguments() != null) {
                            chatToolCall.getFunction().setArguments(mapper.readTree(chatToolCall.getFunction().getArguments().asText()));
                        }
                    }
                }

            }

            return new ChatMessageAccumulator(messageChunk, accumulatedMessage);
        });
    }


}
