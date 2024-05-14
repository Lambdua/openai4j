![Maven Central](https://img.shields.io/maven-central/v/io.github.lambdua/service?color=blue)

# OpenAi4J

OpenAi4J是一个非官方的Java库，旨在帮助java开发者与OpenAI的GPT模型和相关api交互，支持包括最新添加的gpt4 turbo vision。</br>
该库最初从TheoKanning/openai-java派生而来，在最初项目的维护中断后，该库继续开发，以包含最新的 openAi API功能。

## 特性

- 支持所有OpenAI的最新API，包括Completions、Chat、Edits、Embeddings、Audio、Files、Assistants-v2、Images、Moderations、Batch和Fine-tuning。
  Images, Moderations, Batch, and Fine-tuning.
- 易于使用的客户端设置，可立即进行API交互。
- 大量的示例和文档可帮助您快速入门,具体参考example包下的代码。
- API密钥和基本URL的环境变量集成的可定制设置。
- 支持同步和异步、流式API调用。

这个库旨在为Java开发人员提供一个强大的工具，将OpenAI的强大功能毫不费力地集成到他们的应用程序中。

# 快速开始

## 导入依赖
### Gradle

`implementation 'io.github.lambdua:<api|client|service>:0.20.7'`
### Maven
```xml

<dependency>
    <groupId>io.github.lambdua</groupId>
    <artifactId>service</artifactId>
    <version>0.20.7</version>
</dependency>
```

## 和OpenAi模型聊天

```java
static void simpleChat() {
    //从环境变量OPENAI_api_key获取api-key
    OpenAiService service = new OpenAiService(Duration.ofSeconds(30));
    List<ChatMessage> messages = new ArrayList<>();
    ChatMessage systemMessage = new SystemMessage("你是一只可爱的猫，会这样说话。");
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

# 单纯使用POJO

如果您希望开发自己的客户端，只需从api模块导入POJO即可,api模块提供了OpenAi API的接口对象，您可以直接使用这些对象进行开发。

```xml

<dependency>
    <groupId>io.github.lambdua</groupId>
    <artifactId>api</artifactId>
    <version>0.20.7</version>
</dependency>
```

# 更多使用示例

示例代码都在`example`包下，包括了大部分功能的使用。</br>
您可以参考example包下的代码。以下是一些常用功能的使用示例。

<details>
<summary>gpt-vision 图像识别</summary>

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
<summary>自定义 OpenAiService</summary>
OpenAiService的设置选项多样，可以根据需要进行设置，如下所示：

```java
//0 使用默认配置读取环境变量OPENAI-API_KEY、OPENAI-API _BASE-URL作为默认API_KEY和BASE-URL，
//鼓励使用环境变量来加载OpenAI API密钥
OpenAiService openAiService0 = new OpenAiService();
//1.使用默认的baseUrl,默认配置service,这里会默认先从环境变量中获取BaseURL(key:OPENAI_API_BASE_URL),如果没有则使用默认的"https://api.openai.com/v1/";
OpenAiService openAiService = new OpenAiService(API_KEY);
//2. 使用自定义的baseUrl,默认配置配置service
OpenAiService openAiService1 = new OpenAiService(API_KEY, BASE_URL);
//3.自定义过期时间
OpenAiService openAiService2 = new OpenAiService(API_KEY, Duration.ofSeconds(10));
//4. 更灵活的自定义
//4.1. 自定义okHttpClient
OkHttpClient client = new OkHttpClient.Builder()
        //连接池
        .connectionPool(new ConnectionPool(Runtime.getRuntime().availableProcessors() * 2, 30, TimeUnit.SECONDS))
        //自定义的拦截器,如重试拦截器,日志拦截器,负载均衡拦截器等
        // .addInterceptor(new RetryInterceptor())
        // .addInterceptor(new LogInterceptor())
        // .addInterceptor(new LoadBalanceInterceptor())
        //添加代理
        // .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxyHost", 8080)))
        .connectTimeout(2, TimeUnit.SECONDS)
        .writeTimeout(3, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1))
        .build();
//4.2 自定义Retorfit配置
Retrofit retrofit = OpenAiService.defaultRetrofit(client, OpenAiService.defaultObjectMapper(), BASE_URL);
OpenAiApi openAiApi = retrofit.create(OpenAiApi.class);
OpenAiService openAiService3 = new OpenAiService(openAiApi);
```

</details>

<details>
<summary>流式对话</summary>

```java
    static void streamChat() {
    //从环境变量OPENAI_API_KEY获取
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
<summary>Tools 使用</summary>

本库支持过时的function调用方法和当前基于tool的方法。

首先,我们定义一个function对象,定义function对象的方式很灵活,你可以使用pojo定义(json schema自动序列化)
,也可以使用如`map`,`ChatFunctionDynamic`方式去定义,可以参考example包下的代码,这里我们定义一个天气查询的function对象:

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

接下来，我们声明该函数并将其与执行器相关联，在这里模拟API响应：
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

然后，该服务用于聊天完成请求，包含以下工具：

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
<summary>function(deprecated)</summary>

```java
static void functionChat() {
    OpenAiService service = new OpenAiService(Duration.ofSeconds(30));
    final List<ChatMessage> messages = new ArrayList<>();
    final ChatMessage systemMessage = new SystemMessage("You are a helpful assistant.");
    final ChatMessage userMessage = new UserMessage("What is the weather in BeiJin?");
    messages.add(systemMessage);
    messages.add(userMessage);

    ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
            .model("gpt-3.5-turbo")
            .messages(messages)
            .functions(Collections.singletonList(ToolUtil.weatherFunction()))
            .functionCall("auto")
            .n(1)
            .maxTokens(100)
            .build();
    //Request is sent
    ChatCompletionChoice choice = service.createChatCompletion(chatCompletionRequest).getChoices().get(0);
    AssistantMessage functionCallMsg = choice.getMessage();
    ChatFunctionCall functionCall = functionCallMsg.getFunctionCall();
    System.out.println(functionCall);

    messages.add(functionCallMsg);
    messages.add(new FunctionMessage("the weather is fine today.", "get_weather"));

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
<summary>流式对话中调用tool(支持同时调用多个tool)</summary>

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
<summary>Token使用的计算</summary>

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
<summary>Assistant Stream Manager</summary>

通过使用`AssistantEventHandler`类和`AssistantStreamManager`
类，可以更容易地管理Assistant的流式调用。 `AssistantEventHandler`包含了所有的Assistant stream
事件回调钩子,你可以根据需要实现不同的event:

```java
    /**
 * You can implement various event callbacks for Assistant Event Handlers according to your own needs, making it convenient for you to handle various events related to Assistant
 */
private static class LogHandler implements AssistantEventHandler {
  @Override
  public void onEvent(AssistantSSE sse) {
    //每一个事件都会调用这个方法
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

`AssistantStreamManager`
对stream中的各个事件进行编排管理,支持同步/异步获取stream中的内容,可以通过manager获取.下面是一个使用样例,更多样例可以参考`AssistantStreamManagerTest.java`

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

    //一般响应
    Flowable<AssistantSSE> threadAndRunStream = service.createThreadAndRunStream(
            CreateThreadAndRunRequest.builder()
                    .assistantId(assistantId)
                    //这里不使用任何工具
                    .toolChoice(ToolChoice.NONE)
                    .thread(ThreadRequest.builder()
                            .messages(Collections.singletonList(
                                    MessageRequest.builder()
                                            .content("你好,你可以帮助我做什么?")
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

    // 函数调用 stream
    threadId = runStep.getThreadId();
    service.createMessage(threadId, MessageRequest.builder().content("请帮我查询北京天气").build());
    Flowable<AssistantSSE> getWeatherFlowable = service.createRunStream(threadId, RunCreateRequest.builder()
            //这里强制使用get_weather函数
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
    // 提交函数调用结果
    Flowable<AssistantSSE> toolCallResponseFlowable = service.submitToolOutputsStream(threadId, requireActionRun.getId(), SubmitToolOutputsRequest.ofSingletonToolOutput(callId, "北京的天气是晴天"));
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

# 常见问题

<details style="border: 1px solid #aaa; border-radius: 4px; padding: 0.5em;">
<summary style="font-weight: bold; color: #333;">是否可以自定义OpenAI URL或使用代理URL？</summary>
<p style="padding: 0.5em; margin: 0; color: #555;">是的，您可以在构建OpenAiService时指定一个URL，它将作为基本URL。但我们建议使用环境变量OPENAI_API_BASE_URL和OPENAI_PI_KEY来加载OPENAI API密钥。</p>
</details>

<details style="border: 1px solid #aaa; border-radius: 4px; padding: 0.5em;">
<summary style="font-weight: bold; color: #333;">为什么我遇到连接超时？</summary>
<p style="padding: 0.5em; margin: 0; color: #555;">确保您的网络稳定，并且您的OpenAI服务器可以访问。如果您面临网络不稳定，请考虑增加超时时间.</p>
</details>

# 为OpenAi4J做贡献

十分欢迎你对本仓库做出贡献，并一直在寻找使我们的项目变得更好的方法。如果你是有兴趣帮助改进OpenAi4J，以下是您可以贡献的一些方法：

## 提Issue

请使用GitHub Issue页面报告问题。尽可能具体地说明如何重现您的问题，包括操作系统、Java版本和任何相关日志跟踪等详细信息。

## 提交拉取请求

1. 分叉存储库并从`main`创建您的分支。
2. 如果您添加了应该进行测试的代码，请添加测试。
3. 确保您的代码符合现有的样式指南。
4. 为您的提交编写清晰的日志消息。一行消息对于小更改来说是可以的，但是对于更大的更改，应该有详细的描述。
5. 完成拉取请求表单，链接到您的PR地址的任何问题。

## 支持我

希望你觉得这个库有用！觉得还不错，可以给我一个star,我将十分感谢❤️! 您的支持帮助我保持活力并不断改进这个库。随时关注更新，以获取最新的功能和改进。

感谢您对OpenAi4J的支持！

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

# 许可证

Released under the MIT License


