![Maven Central](https://img.shields.io/maven-central/v/io.github.lambdua/service?color=blue)

# OpenAi4J

OpenAi4J is an unofficial Java library tailored to facilitate the interaction with OpenAI's GPT models, including the
newest additions such as gpt4-turbo vision,assistant-v2. Originally forked from TheoKanning/openai-java, this library
continues development to incorporate latest API features after the original project's maintenance was discontinued.

[中文介绍☕](README-zh.md)

## Features

- Full support for all OpenAI API models including Completions, Chat, Edits, Embeddings, Audio, Files, Assistants-v2,
  Images, Moderations, Batch, and Fine-tuning.
- Easy-to-use client setup with Retrofit for immediate API interaction.
- Extensive examples and documentation to help you start quickly.
- Customizable setup with environment variable integration for API keys and base URLs.
- Supports synchronous and asynchronous API calls.

This library aims to provide Java developers with a robust tool to integrate OpenAI's powerful capabilities into their
applications effortlessly.

# Quick Start

## Import
### Gradle

`implementation 'io.github.lambdua:<api|client|service>:0.20.5'`
### Maven
```xml

<dependency>
  <groupId>io.github.lambdua</groupId>
  <artifactId>service</artifactId>
    <version>0.20.5</version>
</dependency>
```

## chat with OpenAi model

```java
static void simpleChat() {
  //api-key get from environment variable OPENAI_API_KEY
  OpenAiService service = new OpenAiService(Duration.ofSeconds(30));
  List<ChatMessage> messages = new ArrayList<>();
  ChatMessage systemMessage = new SystemMessage("You are a cute cat and will speak as such.");
  messages.add(systemMessage);
  ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
          .model("gpt-3.5-turbo")
          .messages(messages)
          .n(1)
          .maxTokens(50)
          .build();
  ChatCompletionResult chatCompletion = service.createChatCompletion(chatCompletionRequest);
  System.out.println(chatCompletion.getChoices().get(0).getMessage().getContent());
}
```

# Just Using POJO

If you wish to develop your own client, simply import POJOs from the api module.</br>
Ensure your client adopts snake case naming for compatibility with the OpenAI API.
To utilize pojos, import the api module:

```xml

<dependency>
  <groupId>io.github.lambdua</groupId>
  <artifactId>api</artifactId>
    <version>0.20.5</version>
</dependency>
```

# other examples:

The sample code is all in the `example` package, which includes most of the functional usage. </br>
You can refer to the code in the example package. Below are some commonly used feature usage examples

<details>
<summary>gpt-vision image recognition</summary>

```java
static void gptVision() {
  OpenAiService service = new OpenAiService(Duration.ofSeconds(20));
  final List<ChatMessage> messages = new ArrayList<>();
  final ChatMessage systemMessage = new SystemMessage("You are a helpful assistant.");
  //Here, the imageMessage is intended for image recognition
  final ChatMessage imageMessage = UserMessage.buildImageMessage("What's in this image?",
          "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg");
  messages.add(systemMessage);
  messages.add(imageMessage);

  ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
          .model("gpt-4-turbo")
          .messages(messages)
          .n(1)
          .maxTokens(200)
          .build();
  ChatCompletionChoice choice = service.createChatCompletion(chatCompletionRequest).getChoices().get(0);
  System.out.println(choice.getMessage().getContent());
}
```

</details>

<details>
<summary>Customizing OpenAiService</summary>
OpenAiService is versatile in its setup options, as demonstrated in the `example.ServiceCreateExample` within the example package.

```java
//0 Using the default configuration, read the environment variables OPENAI-API_KEY, OPENAI-API_BASE-URL as the default API_KEY and BASE-URL,
//encourage the use of environment variables to load the OpenAI API key
OpenAiService openAiService0 = new OpenAiService();
//1.Use the default base URL and configure service by default. Here, the base URL (key: OPENAI API BASE URL) will be obtained from the environment variable by default. If not, the default URL will be used“ https://api.openai.com/v 1/";
OpenAiService openAiService = new OpenAiService(API_KEY);
//2. Use custom base Url with default configuration of service
OpenAiService openAiService1 = new OpenAiService(API_KEY, BASE_URL);
//3.Custom expiration time
OpenAiService openAiService2 = new OpenAiService(API_KEY, Duration.ofSeconds(10));
//4. More flexible customization
//4.1. customize okHttpClient
OkHttpClient client = new OkHttpClient.Builder()
        //connection pool
        .connectionPool(new ConnectionPool(Runtime.getRuntime().availableProcessors() * 2, 30, TimeUnit.SECONDS))
        //Customized interceptors, such as retry interceptors, log interceptors, load balancing interceptors, etc
        // .addInterceptor(new RetryInterceptor())
        // .addInterceptor(new LogInterceptor())
        // .addInterceptor(new LoadBalanceInterceptor())
        // .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxyHost", 8080)))
        .connectTimeout(2, TimeUnit.SECONDS)
        .writeTimeout(3, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1))
        .build();
//4.2 Customizing Retorfit Configuration
Retrofit retrofit = OpenAiService.defaultRetrofit(client, OpenAiService.defaultObjectMapper(), BASE_URL);
OpenAiApi openAiApi = retrofit.create(OpenAiApi.class);
OpenAiService openAiService3 = new OpenAiService(openAiApi);
```

</details>

<details>
<summary>stream chat</summary>

```java
    static void streamChat() {
  //api-key get from environment variable OPENAI_API_KEY
  OpenAiService service = new OpenAiService(Duration.ofSeconds(30));
  List<ChatMessage> messages = new ArrayList<>();
  ChatMessage systemMessage = new SystemMessage("You are a cute cat and will speak as such.");
  messages.add(systemMessage);
  ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
          .model("gpt-3.5-turbo")
          .messages(messages)
          .n(1)
          .maxTokens(50)
          .build();
  service.streamChatCompletion(chatCompletionRequest).blockingForEach(System.out::println);
}
```

</details>

<details>
<summary>Tools</summary>
This library supports both the outdated method of function calls and the current tool-based approach.

First, we define a function object. The definition of a function object is flexible; you can use POJO to define it (
automatically serialized by JSON schema) or use methods like `map` and `FunctionDefinition` to define it. You can refer
to the code in the example package. Here, we define a weather query function object:

```java
public class Weather {
    @JsonPropertyDescription("City and state, for example: León, Guanajuato")
    public String location;
    @JsonPropertyDescription("The temperature unit, can be 'celsius' or 'fahrenheit'")
    @JsonProperty(required = true)
    public WeatherUnit unit;
}
public enum WeatherUnit {
    CELSIUS, FAHRENHEIT;
}
public static class WeatherResponse {
    public String location;
    public WeatherUnit unit;
    public int temperature;
    public String description;
    
    // constructor
}
```

Next, we declare the function and associate it with an executor, here simulating an API response:

```java
//First, a function to fetch the weather
public static FunctionDefinition weatherFunction() {
    return FunctionDefinition.<Weather>builder()
            .name("get_weather")
            .description("Get the current weather in a given location")
            .parametersDefinitionByClass(Weather.class)
            //The executor here is a lambda expression that accepts a Weather object and returns a Weather Response object
            .executor(w -> new WeatherResponse(w.location, w.unit, 25, "sunny"))
            .build();
}
```

Then, the service is used for a chatCompletion request, incorporating the tool:

```java
static void toolChat() {
    OpenAiService service = new OpenAiService(Duration.ofSeconds(30));
    final ChatTool tool = new ChatTool(ToolUtil.weatherFunction());
    final List<ChatMessage> messages = new ArrayList<>();
    final ChatMessage systemMessage = new SystemMessage("You are a helpful assistant.");
    final ChatMessage userMessage = new UserMessage("What is the weather in BeiJin?");
    messages.add(systemMessage);
    messages.add(userMessage);

    ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
            .model("gpt-3.5-turbo")
            .messages(messages)
            //Tools is a list; multiple tools can be included
            .tools(Collections.singletonList(tool))
            .toolChoice(ToolChoice.AUTO)
            .n(1)
            .maxTokens(100)
            .build();
    //Request is sent
    ChatCompletionChoice choice = service.createChatCompletion(chatCompletionRequest).getChoices().get(0);
    AssistantMessage toolCallMsg = choice.getMessage();
    ChatToolCall toolCall = toolCallMsg.getToolCalls().get(0);
    System.out.println(toolCall.getFunction());

    messages.add(toolCallMsg);
    messages.add(new ToolMessage("the weather is fine today.", toolCall.getId()));

    //submit tool call
    ChatCompletionRequest toolCallRequest = ChatCompletionRequest.builder()
            .model("gpt-3.5-turbo")
            .messages(messages)
            .n(1)
            .maxTokens(100)
            .build();
    ChatCompletionChoice toolCallChoice = service.createChatCompletion(toolCallRequest).getChoices().get(0);
    System.out.println(toolCallChoice.getMessage().getContent());
}
```

</details>  

<details>
<summary>stream chat with tool call (support Concurrent tool call)</summary>

```java
void streamChatMultipleToolCalls() {
    final List<FunctionDefinition> functions = Arrays.asList(
            //1. weather query
            FunctionDefinition.<ToolUtil.Weather>builder()
                    .name("get_weather")
                    .description("Get the current weather in a given location")
                    .parametersDefinitionByClass(ToolUtil.Weather.class)
                    .executor( w -> {
                        switch (w.location) {
                            case "tokyo":
                                return new ToolUtil.WeatherResponse(w.location, w.unit, 10, "cloudy");
                            case "san francisco":
                                return new ToolUtil.WeatherResponse(w.location, w.unit, 72, "sunny");
                            case "paris":
                                return new ToolUtil.WeatherResponse(w.location, w.unit, 22, "sunny");
                            default:
                                return new ToolUtil.WeatherResponse(w.location, w.unit, 0, "unknown");
                        }
                    }).build(),
            //2. city query
            FunctionDefinition.<ToolUtil.City>builder().name("getCities").description("Get a list of cities by time").parametersDefinitionByClass(ToolUtil.City.class).executor(v -> Arrays.asList("tokyo", "paris")).build()
    );
    final FunctionExecutorManager toolExecutor = new FunctionExecutorManager(functions);

    List<ChatTool> tools = new ArrayList<>();
    tools.add(new ChatTool(functions.get(0)));
    tools.add(new ChatTool(functions.get(1)));

    final List<ChatMessage> messages = new ArrayList<>();
    final ChatMessage systemMessage = new SystemMessage("You are a helpful assistant.");
    final ChatMessage userMessage = new UserMessage("What is the weather like in cities with weather on 2022-12-01 ?");
    messages.add(systemMessage);
    messages.add(userMessage);

    ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
            .builder()
            .model("gpt-3.5-turbo")
            .messages(messages)
            .tools(tools)
            .toolChoice(ToolChoice.AUTO)
            .n(1)
            .maxTokens(200)
            .build();

    AssistantMessage accumulatedMessage = service.mapStreamToAccumulator(service.streamChatCompletion(chatCompletionRequest))
            .blockingLast()
            .getAccumulatedMessage();

    List<ChatToolCall> toolCalls = accumulatedMessage.getToolCalls();

    ChatToolCall toolCall = toolCalls.get(0);
    ChatFunctionCall function = toolCall.getFunction();
    JsonNode jsonNode = toolExecutor.executeAndConvertToJson(function.getName(), function.getArguments());
    ToolMessage toolMessage = toolExecutor.executeAndConvertToChatMessage(function.getName(),function.getArguments(), toolCall.getId());
    messages.add(accumulatedMessage);
    messages.add(toolMessage);
    ChatCompletionRequest chatCompletionRequest2 = ChatCompletionRequest
            .builder()
            //3.5 there may be logical issues
            .model("gpt-3.5-turbo-0125")
            .messages(messages)
            .tools(tools)
            .toolChoice(ToolChoice.AUTO)
            .n(1)
            .maxTokens(100)
            .logitBias(new HashMap<>())
            .build();

    // ChatCompletionChoice choice2 = service.createChatCompletion(chatCompletionRequest2).getChoices().get(0);
    AssistantMessage accumulatedMessage2 = service.mapStreamToAccumulator(service.streamChatCompletion(chatCompletionRequest2))
            .blockingLast()
            .getAccumulatedMessage();
    messages.add(accumulatedMessage2);
    for (ChatToolCall weatherToolCall : accumulatedMessage2.getToolCalls()) {
        ChatFunctionCall call2 = weatherToolCall.getFunction();
        Object itemResult = toolExecutor.execute(call2.getName(), call2.getArguments());
        messages.add(toolExecutor.executeAndConvertToChatMessage(call2.getName(),call2.getArguments(), weatherToolCall.getId()));
    }

    ChatCompletionRequest chatCompletionRequest3 = ChatCompletionRequest
            .builder()
            .model("gpt-3.5-turbo")
            .messages(messages)
            .tools(tools)
            .toolChoice(ToolChoice.AUTO)
            .n(1)
            .maxTokens(100)
            .logitBias(new HashMap<>())
            .build();

    AssistantMessage accumulatedMessage3 = service.mapStreamToAccumulator(service.streamChatCompletion(chatCompletionRequest3))
            .blockingLast()
            .getAccumulatedMessage();
}

```

</details>
<details>
<summary>Token usage calculate</summary>

```java
public static void main(String... args) {
  List<ChatMessage> messages = new ArrayList<>();
  messages.add(new SystemMessage("Hello OpenAI 1."));
  messages.add(new SystemMessage("Hello OpenAI 2.   "));
  messages.add(new UserMessage(Arrays.asList(new ImageContent("text", "", new ImageUrl("dddd")))));
  int tokens_1 = TikTokensUtil.tokens(TikTokensUtil.ModelEnum.GPT_3_5_TURBO.getName(), messages);
  int tokens_2 = TikTokensUtil.tokens(TikTokensUtil.ModelEnum.GPT_3_5_TURBO.getName(), "Hello OpenAI 1.");
  int tokens_3 = TikTokensUtil.tokens(TikTokensUtil.ModelEnum.GPT_4_TURBO.getName(), messages);
}
```

</details>

<details>
<summary>Assistant Tool Call</summary>

```java
static void assistantToolCall() {
    OpenAiService service = new OpenAiService();
    FunctionExecutorManager executor = new FunctionExecutorManager(Collections.singletonList(ToolUtil.weatherFunction()));
    AssistantRequest assistantRequest = AssistantRequest.builder()
            .model("gpt-3.5-turbo").name("weather assistant")
            .instructions("You are a weather assistant responsible for calling the weather API to return weather information based on the location entered by the user")
            .tools(Collections.singletonList(new FunctionTool(ToolUtil.weatherFunction())))
            .temperature(0D)
            .build();
    Assistant assistant = service.createAssistant(assistantRequest);
    String assistantId = assistant.getId();
    ThreadRequest threadRequest = ThreadRequest.builder().build();
    Thread thread = service.createThread(threadRequest);
    String threadId = thread.getId();

    MessageRequest messageRequest = MessageRequest.builder()
            .content("What's the weather of Xiamen?")
            .build();
    //add message to thread
    service.createMessage(threadId, messageRequest);
    RunCreateRequest runCreateRequest = RunCreateRequest.builder().assistantId(assistantId).build();

    Run run = service.createRun(threadId, runCreateRequest);

    Run retrievedRun = service.retrieveRun(threadId, run.getId());
    while (!(retrievedRun.getStatus().equals("completed"))
            && !(retrievedRun.getStatus().equals("failed"))
            && !(retrievedRun.getStatus().equals("expired"))
            && !(retrievedRun.getStatus().equals("incomplete"))
            && !(retrievedRun.getStatus().equals("requires_action"))) {
        retrievedRun = service.retrieveRun(threadId, run.getId());
    }
    System.out.println(retrievedRun);

    RequiredAction requiredAction = retrievedRun.getRequiredAction();
    List<ToolCall> toolCalls = requiredAction.getSubmitToolOutputs().getToolCalls();
    ToolCall toolCall = toolCalls.get(0);
    ToolCallFunction function = toolCall.getFunction();
    String toolCallId = toolCall.getId();

    SubmitToolOutputsRequest submitToolOutputsRequest = SubmitToolOutputsRequest.ofSingletonToolOutput(toolCallId, executor.executeAndConvertToJson(function.getName(),function.getArguments()).toPrettyString());
    retrievedRun = service.submitToolOutputs(threadId, retrievedRun.getId(), submitToolOutputsRequest);

    while (!(retrievedRun.getStatus().equals("completed"))
            && !(retrievedRun.getStatus().equals("failed"))
            && !(retrievedRun.getStatus().equals("expired"))
            && !(retrievedRun.getStatus().equals("incomplete"))
            && !(retrievedRun.getStatus().equals("requires_action"))) {
        retrievedRun = service.retrieveRun(threadId, run.getId());
    }

    System.out.println(retrievedRun);

    OpenAiResponse<Message> response = service.listMessages(threadId, MessageListSearchParameters.builder()
            .runId(retrievedRun.getId()).build());
    List<Message> messages = response.getData();
    messages.forEach(message -> {
        System.out.println(message.getContent());
    });

}
```

</details>

<details>
<summary>Assistant Stream </summary>

```java
static void assistantStream() throws JsonProcessingException {
  OpenAiService service = new OpenAiService();
  String assistantId;
  String threadId;

  AssistantRequest assistantRequest = AssistantRequest.builder()
          .model("gpt-3.5-turbo").name("weather assistant")
          .instructions("You are a weather assistant responsible for calling the weather API to return weather information based on the location entered by the user")
          .tools(Collections.singletonList(new FunctionTool(ToolUtil.weatherFunction())))
          .temperature(0D)
          .build();
  Assistant assistant = service.createAssistant(assistantRequest);
  assistantId = assistant.getId();

    //general response
  Flowable<AssistantSSE> threadAndRunStream = service.createThreadAndRunStream(
          CreateThreadAndRunRequest.builder()
                  .assistantId(assistantId)
                  //no tools are used here
                  .toolChoice(ToolChoice.NONE)
                  .thread(ThreadRequest.builder()
                          .messages(Collections.singletonList(
                                  MessageRequest.builder()
                                          .content("hello what can you help me with?")
                                          .build()
                          ))
                          .build())
                  .build()
  );

  ObjectMapper objectMapper = new ObjectMapper();
  TestSubscriber<AssistantSSE> subscriber1 = new TestSubscriber<>();
  threadAndRunStream
          .doOnNext(System.out::println)
          .blockingSubscribe(subscriber1);

  Optional<AssistantSSE> runStepCompletion = subscriber1.values().stream().filter(item -> item.getEvent().equals(StreamEvent.THREAD_RUN_STEP_COMPLETED)).findFirst();
  RunStep runStep = objectMapper.readValue(runStepCompletion.get().getData(), RunStep.class);
  System.out.println(runStep.getStepDetails());

    // Function call stream
  threadId = runStep.getThreadId();
  service.createMessage(threadId, MessageRequest.builder().content("Please help me check the weather in Beijing").build());
  Flowable<AssistantSSE> getWeatherFlowable = service.createRunStream(threadId, RunCreateRequest.builder()
          //Force the use of the get weather function here
          .assistantId(assistantId)
          .toolChoice(new ToolChoice(new Function("get_weather")))
          .build()
  );

  TestSubscriber<AssistantSSE> subscriber2 = new TestSubscriber<>();
  getWeatherFlowable
          .doOnNext(System.out::println)
          .blockingSubscribe(subscriber2);

  AssistantSSE requireActionSse = subscriber2.values().get(subscriber2.values().size() - 2);
  Run requireActionRun = objectMapper.readValue(requireActionSse.getData(), Run.class);
  RequiredAction requiredAction = requireActionRun.getRequiredAction();
  List<ToolCall> toolCalls = requiredAction.getSubmitToolOutputs().getToolCalls();
  ToolCall toolCall = toolCalls.get(0);
  String callId = toolCall.getId();

  System.out.println(toolCall.getFunction());
    // Submit function call results
    Flowable<AssistantSSE> toolCallResponseFlowable = service.submitToolOutputsStream(threadId, requireActionRun.getId(), SubmitToolOutputsRequest.ofSingletonToolOutput(callId, "The weather in Beijing is sunny"));
  TestSubscriber<AssistantSSE> subscriber3 = new TestSubscriber<>();
  toolCallResponseFlowable
          .doOnNext(System.out::println)
          .blockingSubscribe(subscriber3);

  Optional<AssistantSSE> msgSse = subscriber3.values().stream().filter(item -> StreamEvent.THREAD_MESSAGE_COMPLETED.equals(item.getEvent())).findFirst();
  Message message = objectMapper.readValue(msgSse.get().getData(), Message.class);
  String responseContent = message.getContent().get(0).getText().getValue();
  System.out.println(responseContent);
}
```

</details>


<details>
<summary>Assistant Stream Manager</summary>

By using the `AssistantEventHandler` class and the `AssistantStreamManager` class, it is easier to manage the streaming
calls of Assistant `AssistantEventHandler` contains all Assistant stream event callback hooks, and you can implement
different events as needed:

```java
    /**
     * You can implement various event callbacks for Assistant Event Handlers according to your own needs, making it convenient for you to handle various events related to Assistant
     */
    private static class LogHandler implements AssistantEventHandler {
        @Override
        public void onEvent(AssistantSSE sse) {
            //every event will call this method
        }

        @Override
        public void onRunCreated(Run run) {
            System.out.println("start run: " + run.getId());
        }

        @Override
        public void onEnd() {
            System.out.println("stream end");
        }

        @Override
        public void onMessageDelta(MessageDelta messageDelta) {
            System.out.println(messageDelta.getDelta().getContent().get(0).getText());
        }

        @Override
        public void onMessageCompleted(Message message) {
            System.out.println("message completed");
        }

        @Override
        public void onMessageInComplete(Message message) {
            System.out.println("message in complete");
        }

        @Override
        public void onError(Throwable error) {
            System.out.println("error:" + error.getMessage());
        }
    }
```

`AssistantStreamManager` arranges and manages various events in the stream, supporting synchronous/asynchronous
retrieval of content from the stream,
which can be obtained through the manager. Below is a usage example, for more examples, please refer
to `AssistantStreamManagerTest.java`.

```java
    static void streamTest() {
    OpenAiService service = new OpenAiService();
    //1. create assistant
    AssistantRequest assistantRequest = AssistantRequest.builder()
            .model("gpt-3.5-turbo").name("weather assistant")
            .instructions("You are a weather assistant responsible for calling the weather API to return weather information based on the location entered by the user")
            .tools(Collections.singletonList(new FunctionTool(ToolUtil.weatherFunction())))
            .temperature(0D)
            .build();
    Assistant assistant = service.createAssistant(assistantRequest);
    String assistantId = assistant.getId();

    System.out.println("assistantId:" + assistantId);
    ThreadRequest threadRequest = ThreadRequest.builder()
            .build();
    Thread thread = service.createThread(threadRequest);
    String threadId = thread.getId();
    System.out.println("threadId:" + threadId);
    MessageRequest messageRequest = MessageRequest.builder()
            .content("What can you help me with?")
            .build();
    service.createMessage(threadId, messageRequest);
    RunCreateRequest runCreateRequest = RunCreateRequest.builder()
            .assistantId(assistantId)
            .toolChoice(ToolChoice.AUTO)
            .build();

    //blocking
    // AssistantStreamManager blockedManagere = AssistantStreamManager.syncStart(service.createRunStream(threadId, runCreateRequest), new LogHandler());
    //async
    AssistantStreamManager streamManager = AssistantStreamManager.start(service.createRunStream(threadId, runCreateRequest), new LogHandler());


    //Other operations can be performed here...
    boolean completed = streamManager.isCompleted();


    // you can shut down the streamManager if you want to stop the stream
    streamManager.shutDown();

    //waiting for completion
    streamManager.waitForCompletion();
    // all of flowable events
    List<AssistantSSE> eventMsgsHolder = streamManager.getEventMsgsHolder();

    Optional<Run> currentRun = streamManager.getCurrentRun();
    // get the accumulated message
    streamManager.getAccumulatedMsg().ifPresent(msg -> {
        System.out.println("accumulatedMsg:" + msg);
    });
    service.deleteAssistant(assistantId);
    service.deleteThread(threadId);
}
```

</details>

- [Assistant iamge chat](./service/src/test/java/com/theokanning/openai/service/assistants/AssistantImageTest.java#L65-L90)

# FAQs

<details style="border: 1px solid #aaa; border-radius: 4px; padding: 0.5em;">
<summary style="font-weight: bold; color: #333;">Is it possible to customize the OpenAI URL or use a proxy URL?</summary>
<p style="padding: 0.5em; margin: 0; color: #555;">Yes, you can specify a URL when constructing OpenAiService, which will serve as the base URL.But we recommend using the
environment variable OPENAI_API_BASE_URL and OPENAI_API_KEY to load the OpenAI API key.</p>
</details>

<details style="border: 1px solid #aaa; border-radius: 4px; padding: 0.5em;">
<summary style="font-weight: bold; color: #333;">Why am I experiencing connection timeouts?</summary>
<p style="padding: 0.5em; margin: 0; color: #555;">Ensure your network is stable and your OpenAI server is accessible. If you face network instability, consider increasing the timeout duration.</p>
</details>

# Contributing to OpenAi4J

We welcome contributions from the community and are always looking for ways to make our project better. If you're
interested in helping improve OpenAi4J, here are some ways you can contribute:

## Reporting Issues

Please use the GitHub Issues page to report issues. Be as specific as possible about how to reproduce the problem you
are having, and include details like your operating system, Java version, and any relevant stack traces.

## Submitting Pull Requests

1. Fork the repository and create your branch from `main`.
2. If you've added code that should be tested, add tests.
3. Ensure your code lints and adheres to the existing style guidelines.
4. Write a clear log message for your commits. One-line messages are fine for small changes, but bigger changes should
   have detailed descriptions.
5. Complete the pull request form, linking to any issues your PR addresses.

## Support Us

We hope you find this library useful! If you do, consider giving it a star on library❤️❤️❤️. Your support helps us keep
the project alive and continuously improve it. Stay tuned for updates and feel free to contribute to the project or
suggest new features.
Thank you for supporting OpenAi4J!

# Contributors

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

<a href="https://github.com/Lambdua/openai4j/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=Lambdua/openai4j" />
</a>

# License
Released under the MIT License


