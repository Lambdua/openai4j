package com.theokanning.openai.client;

import com.theokanning.openai.DeleteResult;
import com.theokanning.openai.OpenAiResponse;
import com.theokanning.openai.assistants.assistant.*;
import com.theokanning.openai.assistants.message.Message;
import com.theokanning.openai.assistants.message.MessageFile;
import com.theokanning.openai.assistants.message.MessageRequest;
import com.theokanning.openai.assistants.message.ModifyMessageRequest;
import com.theokanning.openai.assistants.run.*;
import com.theokanning.openai.assistants.thread.Thread;
import com.theokanning.openai.assistants.thread.ThreadRequest;
import com.theokanning.openai.assistants.vector_store.ModifyVectorStoreRequest;
import com.theokanning.openai.assistants.vector_store.VectorStore;
import com.theokanning.openai.assistants.vector_store.VectorStoreRequest;
import com.theokanning.openai.assistants.vector_store_file.VectorStoreFile;
import com.theokanning.openai.assistants.vector_store_file_batch.VectorStoreFilesBatch;
import com.theokanning.openai.assistants.vector_store_file_batch.VectorStoreFilesBatchRequest;
import com.theokanning.openai.audio.CreateSpeechRequest;
import com.theokanning.openai.audio.TranscriptionResult;
import com.theokanning.openai.audio.TranslationResult;
import com.theokanning.openai.billing.BillingUsage;
import com.theokanning.openai.billing.Subscription;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.CompletionResult;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.edit.EditRequest;
import com.theokanning.openai.edit.EditResult;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResult;
import com.theokanning.openai.file.File;
import com.theokanning.openai.fine_tuning.FineTuningEvent;
import com.theokanning.openai.fine_tuning.FineTuningJob;
import com.theokanning.openai.fine_tuning.FineTuningJobRequest;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.ImageResult;
import com.theokanning.openai.model.Model;
import com.theokanning.openai.moderation.ModerationRequest;
import com.theokanning.openai.moderation.ModerationResult;
import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.time.LocalDate;
import java.util.Map;

public interface OpenAiApi {

    @GET("models")
    Single<OpenAiResponse<Model>> listModels();

    @GET("models/{model_id}")
    Single<Model> getModel(@Path("model_id") String modelId);

    @POST("completions")
    Single<CompletionResult> createCompletion(@Body CompletionRequest request);

    @Streaming
    @POST("completions")
    Call<ResponseBody> createCompletionStream(@Body CompletionRequest request);

    @POST("chat/completions")
    Single<ChatCompletionResult> createChatCompletion(@Body ChatCompletionRequest request);

    @Streaming
    @POST("chat/completions")
    Call<ResponseBody> createChatCompletionStream(@Body ChatCompletionRequest request);


    @POST("edits")
    Single<EditResult> createEdit(@Body EditRequest request);


    @POST("embeddings")
    Single<EmbeddingResult> createEmbeddings(@Body EmbeddingRequest request);


    @GET("files")
    Single<OpenAiResponse<File>> listFiles();

    @Multipart
    @POST("files")
    Single<File> uploadFile(@Part("purpose") RequestBody purpose, @Part MultipartBody.Part file);

    @DELETE("files/{file_id}")
    Single<DeleteResult> deleteFile(@Path("file_id") String fileId);

    @GET("files/{file_id}")
    Single<File> retrieveFile(@Path("file_id") String fileId);

    @Streaming
    @GET("files/{file_id}/content")
    Single<ResponseBody> retrieveFileContent(@Path("file_id") String fileId);

    @POST("fine_tuning/jobs")
    Single<FineTuningJob> createFineTuningJob(@Body FineTuningJobRequest request);

    @GET("fine_tuning/jobs")
    Single<OpenAiResponse<FineTuningJob>> listFineTuningJobs();

    @GET("fine_tuning/jobs/{fine_tuning_job_id}")
    Single<FineTuningJob> retrieveFineTuningJob(@Path("fine_tuning_job_id") String fineTuningJobId);

    @POST("fine_tuning/jobs/{fine_tuning_job_id}/cancel")
    Single<FineTuningJob> cancelFineTuningJob(@Path("fine_tuning_job_id") String fineTuningJobId);

    @GET("fine_tuning/jobs/{fine_tuning_job_id}/events")
    Single<OpenAiResponse<FineTuningEvent>> listFineTuningJobEvents(@Path("fine_tuning_job_id") String fineTuningJobId);

    @POST("completions")
    Single<CompletionResult> createFineTuneCompletion(@Body CompletionRequest request);

    @DELETE("models/{fine_tune_id}")
    Single<DeleteResult> deleteFineTune(@Path("fine_tune_id") String fineTuneId);

    @POST("images/generations")
    Single<ImageResult> createImage(@Body CreateImageRequest request);

    @POST("images/edits")
    Single<ImageResult> createImageEdit(@Body RequestBody requestBody);

    @POST("images/variations")
    Single<ImageResult> createImageVariation(@Body RequestBody requestBody);

    @POST("audio/transcriptions")
    Single<TranscriptionResult> createTranscription(@Body RequestBody requestBody);

    @POST("audio/translations")
    Single<TranslationResult> createTranslation(@Body RequestBody requestBody);

    @POST("audio/speech")
    Single<ResponseBody> createSpeech(@Body CreateSpeechRequest requestBody);

    @POST("moderations")
    Single<ModerationResult> createModeration(@Body ModerationRequest request);

    /**
     * Account information inquiry: It contains total amount (in US dollars) and other information.
     *
     * @return
     */
    @Deprecated
    @GET("dashboard/billing/subscription")
    Single<Subscription> subscription();

    /**
     * Account call interface consumption amount inquiry.
     * totalUsage = Total amount used by the account (in US cents).
     *
     * @param starDate
     * @param endDate
     * @return Consumption amount information.
     */
    @Deprecated
    @GET("dashboard/billing/usage")
    Single<BillingUsage> billingUsage(@Query("start_date") LocalDate starDate, @Query("end_date") LocalDate endDate);


    @Headers({"OpenAI-Beta: assistants=v1"})
    @POST("assistants")
    Single<Assistant> createAssistant(@Body AssistantRequest request);


    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("assistants")
    Single<OpenAiResponse<Assistant>> listAssistants(@QueryMap Map<String, Object> filterRequest);


    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("assistants/{assistant_id}")
    Single<Assistant> retrieveAssistant(@Path("assistant_id") String assistantId);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @POST("assistants/{assistant_id}")
    Single<Assistant> modifyAssistant(@Path("assistant_id") String assistantId, @Body ModifyAssistantRequest request);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @DELETE("assistants/{assistant_id}")
    Single<DeleteResult> deleteAssistant(@Path("assistant_id") String assistantId);

    //----

    @Headers({"OpenAI-Beta: assistants=v1"})
    @POST("assistants/{assistant_id}/files")
    Single<AssistantFile> createAssistantFile(@Path("assistant_id") String assistantId, @Body AssistantFileRequest fileRequest);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("assistants/{assistant_id}/files/{file_id}")
    Single<AssistantFile> retrieveAssistantFile(@Path("assistant_id") String assistantId, @Path("file_id") String fileId);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @DELETE("assistants/{assistant_id}/files/{file_id}")
    Single<DeleteResult> deleteAssistantFile(@Path("assistant_id") String assistantId, @Path("file_id") String fileId);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("assistants/{assistant_id}/files")
    Single<OpenAiResponse<AssistantFile>> listAssistantFiles(@Path("assistant_id") String assistantId, @QueryMap Map<String, Object> filterRequest);

    //---


    @Headers({"OpenAI-Beta: assistants=v1"})
    @POST("threads")
    Single<Thread> createThread(@Body ThreadRequest request);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("threads/{thread_id}")
    Single<Thread> retrieveThread(@Path("thread_id") String threadId);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @POST("threads/{thread_id}")
    Single<Thread> modifyThread(@Path("thread_id") String threadId, @Body ThreadRequest request);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @DELETE("threads/{thread_id}")
    Single<DeleteResult> deleteThread(@Path("thread_id") String threadId);


    @Headers({"OpenAI-Beta: assistants=v1"})
    @POST("threads/{thread_id}/messages")
    Single<Message> createMessage(@Path("thread_id") String threadId, @Body MessageRequest request);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("threads/{thread_id}/messages/{message_id}")
    Single<Message> retrieveMessage(@Path("thread_id") String threadId, @Path("message_id") String messageId);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @POST("threads/{thread_id}/messages/{message_id}")
    Single<Message> modifyMessage(@Path("thread_id") String threadId, @Path("message_id") String messageId, @Body ModifyMessageRequest request);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("threads/{thread_id}/messages")
    Single<OpenAiResponse<Message>> listMessages(@Path("thread_id") String threadId);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("threads/{thread_id}/messages")
    Single<OpenAiResponse<Message>> listMessages(@Path("thread_id") String threadId, @QueryMap Map<String, Object> filterRequest);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("threads/{thread_id}/messages/{message_id}/files/{file_id}")
    Single<MessageFile> retrieveMessageFile(@Path("thread_id") String threadId, @Path("message_id") String messageId, @Path("file_id") String fileId);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("threads/{thread_id}/messages/{message_id}/files")
    Single<OpenAiResponse<MessageFile>> listMessageFiles(@Path("thread_id") String threadId, @Path("message_id") String messageId);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("threads/{thread_id}/messages/{message_id}/files")
    Single<OpenAiResponse<MessageFile>> listMessageFiles(@Path("thread_id") String threadId, @Path("message_id") String messageId, @QueryMap Map<String, Object> filterRequest);

    @Headers("OpenAI-Beta: assistants=v1")
    @POST("threads/{thread_id}/runs")
    Single<Run> createRun(@Path("thread_id") String threadId, @Body RunCreateRequest runCreateRequest);

    @Headers("OpenAI-Beta: assistants=v1")
    @GET("threads/{thread_id}/runs/{run_id}")
    Single<Run> retrieveRun(@Path("thread_id") String threadId, @Path("run_id") String runId);

    @Headers("OpenAI-Beta: assistants=v1")
    @POST("threads/{thread_id}/runs/{run_id}")
    Single<Run> modifyRun(@Path("thread_id") String threadId, @Path("run_id") String runId, @Body Map<String, String> metadata);

    @Headers("OpenAI-Beta: assistants=v1")
    @GET("threads/{thread_id}/runs")
    Single<OpenAiResponse<Run>> listRuns(@Path("thread_id") String threadId, @QueryMap Map<String, String> listSearchParameters);


    @Headers("OpenAI-Beta: assistants=v1")
    @POST("threads/{thread_id}/runs/{run_id}/submit_tool_outputs")
    Single<Run> submitToolOutputs(@Path("thread_id") String threadId, @Path("run_id") String runId, @Body SubmitToolOutputsRequest submitToolOutputsRequest);


    @Headers("OpenAI-Beta: assistants=v1")
    @POST("threads/{thread_id}/runs/{run_id}/cancel")
    Single<Run> cancelRun(@Path("thread_id") String threadId, @Path("run_id") String runId);

    @Headers("OpenAI-Beta: assistants=v1")
    @POST("threads/runs")
    Single<Run> createThreadAndRun(@Body CreateThreadAndRunRequest createThreadAndRunRequest);

    @Headers("OpenAI-Beta: assistants=v1")
    @GET("threads/{thread_id}/runs/{run_id}/steps/{step_id}")
    Single<RunStep> retrieveRunStep(@Path("thread_id") String threadId, @Path("run_id") String runId, @Path("step_id") String stepId);

    @Headers("OpenAI-Beta: assistants=v1")
    @GET("threads/{thread_id}/runs/{run_id}/steps")
    Single<OpenAiResponse<RunStep>> listRunSteps(@Path("thread_id") String threadId, @Path("run_id") String runId, @QueryMap Map<String, String> listSearchParameters);

    @Headers("OpenAI-Beta: assistants=v1")
    @POST("vector_stores")
    Single<VectorStore> createVectorStore(@Body VectorStoreRequest request);

    @Headers("OpenAI-Beta: assistants=v1")
    @GET("vector_stores")
    Single<OpenAiResponse<VectorStore>> listVectorStores(@QueryMap Map<String, Object> filterRequest);

    @Headers("OpenAI-Beta: assistants=v1")
    @GET("vector_stores/{vector_store_id}")
    Single<VectorStore> retrieveVectorStore(@Path("vector_store_id") String vectorStoreId);

    @Headers("OpenAI-Beta: assistants=v1")
    @POST("vector_stores/{vector_store_id}")
    Single<VectorStore> modifyVectorStore(@Path("vector_store_id") String vectorStoreId, @Body ModifyVectorStoreRequest request);

    @Headers("OpenAI-Beta: assistants=v1")
    @DELETE("vector_stores/{vector_store_id}")
    Single<DeleteResult> deleteVectorStore(@Path("vector_store_id") String vectorStoreId);

    @Headers("OpenAI-Beta: assistants=v1")
    @POST("vector_stores/{vector_store_id}/files")
    Single<VectorStoreFile> createVectorStoreFile(@Path("vector_store_id") String vectorStoreId, @Body AssistantFileRequest fileRequest);

    @Headers("OpenAI-Beta: assistants=v1")
    @GET("vector_stores/{vector_store_id}/files")
    Single<OpenAiResponse<VectorStoreFile>> listVectorStoreFiles(@Path("vector_store_id") String vectorStoreId, @QueryMap Map<String, Object> filterRequest);

    @Headers("OpenAI-Beta: assistants=v1")
    @DELETE("vector_stores/{vector_store_id}/files/{file_id}")
    Single<DeleteResult> deleteVectorStoreFile(@Path("vector_store_id") String vectorStoreId, @Path("file_id") String fileId);

    @Headers("OpenAI-Beta: assistants=v1")
    @POST("vector_stores/{vector_store_id}/file_batches")
    Single<VectorStoreFilesBatch> createVectorStoreFileBatch(@Path("vector_store_id") String vectorStoreId, @Body VectorStoreFilesBatchRequest request);

    @Headers("OpenAI-Beta: assistants=v1")
    @GET("vector_stores/{vector_store_id}/file_batches/{batch_id}")
    Single<VectorStoreFilesBatch> retrieveVectorStoreFileBatch(@Path("vector_store_id") String vectorStoreId, @Path("batch_id") String batchId);

    @Headers("OpenAI-Beta: assistants=v1")
    @POST("vector_stores/{vector_store_id}/file_batches/{batch_id}/cancel")
    Single<VectorStoreFilesBatch> cancelVectorStoreFileBatch(@Path("vector_store_id") String vectorStoreId, @Path("batch_id") String batchId);

    @Headers("OpenAI-Beta: assistants=v1")
    @GET("vector_stores/{vector_store_id}/file_batches/{batch_id}/files")
    Single<OpenAiResponse<VectorStoreFile>> listVectorStoreFilesInBatch(@Path("vector_store_id") String vectorStoreId, @Path("batch_id") String batchId, @QueryMap Map<String, Object> filterRequest);
}
