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

## v0.20.1 will be released soon

- [ ]  a more user-friendly way to access and use assistant-stream

# Quick Start

## Import
### Gradle
`implementation 'io.github.lambdua:<api|client|service>:0.20.0'`
### Maven
```xml

<dependency>
  <groupId>io.github.lambdua</groupId>
  <artifactId>service</artifactId>
  <version>0.20.0</version>
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
  <version>0.20.0</version>
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
Firstly, we define the function parameters:

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
ChatFunction function = ChatFunction.builder()
        .name("get_weather")
        .description("Get the current weather in a specified location")
        //The executor is a lambda expression that takes a Weather object and returns a WeatherResponse
        .executor(Weather.class, w -> new WeatherResponse(w.location, w.unit, 25, "sunny"))
        .build();
```

Then, the service is used for a chatCompletion request, incorporating the tool:

```java
static void toolChat() {
  OpenAiService service = new OpenAiService(Duration.ofSeconds(30));
  //ToolUtil is a utility class that simplifies the creation of tools
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
<summary>stream chat with tool call (support Concurrent tool call)</summary>

```java
void streamChatMultipleToolCalls() {
  final List<ChatFunction> functions = Arrays.asList(
          //1. 天气查询
          ChatFunction.builder()
                  .name("get_weather")
                  .description("Get the current weather in a given location")
                  .executor(Weather.class, w -> {
                    switch (w.location) {
                      case "tokyo":
                        return new WeatherResponse(w.location, w.unit, 10, "cloudy");
                      case "san francisco":
                        return new WeatherResponse(w.location, w.unit, 72, "sunny");
                      case "paris":
                        return new WeatherResponse(w.location, w.unit, 22, "sunny");
                      default:
                        return new WeatherResponse(w.location, w.unit, 0, "unknown");
                    }
                  }).build(),
          //2. 城市查询
          ChatFunction.builder().name("getCities").description("Get a list of cities by time").executor(City.class, v -> Arrays.asList("tokyo", "paris")).build()
  );
  final FunctionExecutor toolExecutor = new FunctionExecutor(functions);

  List<ChatTool> tools = new ArrayList<>();
  tools.add(new ChatTool<>(functions.get(0)));
  tools.add(new ChatTool<>(functions.get(1)));

  final List<ChatMessage> messages = new ArrayList<>();
  final ChatMessage systemMessage = new SystemMessage("You are a helpful assistant.");
  final ChatMessage userMessage = new UserMessage("What is the weather like in cities with weather on 2022-12-01 ?");
  messages.add(systemMessage);
  messages.add(userMessage);

  ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
          .builder()
          .model("gpt-3.5-turbo-0613")
          .messages(messages)
          .tools(tools)
          .toolChoice("auto")
          .n(1)
          .maxTokens(200)
          .build();

  AssistantMessage accumulatedMessage = service.mapStreamToAccumulator(service.streamChatCompletion(chatCompletionRequest))
          .blockingLast()
          .getAccumulatedMessage();

  List<ChatToolCall> toolCalls = accumulatedMessage.getToolCalls();
  ChatToolCall toolCall = toolCalls.get(0);
  Object execute = toolExecutor.execute(toolCall.getFunction());
  JsonNode jsonNode = toolExecutor.executeAndConvertToJson(toolCall.getFunction());
  ToolMessage toolMessage = toolExecutor.executeAndConvertToMessageHandlingExceptions(toolCall.getFunction(), toolCall.getId());
  messages.add(accumulatedMessage);
  messages.add(toolMessage);

  ChatCompletionRequest chatCompletionRequest2 = ChatCompletionRequest
          .builder()
          //3.5 there may be logical issues
          .model("gpt-3.5-turbo-0125")
          .messages(messages)
          .tools(tools)
          .toolChoice("auto")
          .n(1)
          .maxTokens(100)
          .logitBias(new HashMap<>())
          .build();

  // ChatCompletionChoice choice2 = service.createChatCompletion(chatCompletionRequest2).getChoices().get(0);
  AssistantMessage accumulatedMessage2 = service.mapStreamToAccumulator(service.streamChatCompletion(chatCompletionRequest2))
          .blockingLast()
          .getAccumulatedMessage();
  //这里应该有两个工具调用
  messages.add(accumulatedMessage2);

  for (ChatToolCall weatherToolCall : accumulatedMessage2.getToolCalls()) {
    Object itemResult = toolExecutor.execute(weatherToolCall.getFunction());
    assertInstanceOf(WeatherResponse.class, itemResult);
    messages.add(toolExecutor.executeAndConvertToMessage(weatherToolCall.getFunction(), weatherToolCall.getId()));
  }

  ChatCompletionRequest chatCompletionRequest3 = ChatCompletionRequest
          .builder()
          .model("gpt-3.5-turbo-0613")
          .messages(messages)
          .tools(tools)
          .toolChoice("auto")
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
  FunctionExecutor executor = new FunctionExecutor(Collections.singletonList(ToolUtil.weatherFunction()));
  //create assistant
  AssistantRequest assistantRequest = AssistantRequest.builder()
          .model("gpt-3.5-turbo").name("weather assistant")
          .instructions("You are a weather assistant responsible for calling the weather API to return weather information based on the location entered by the user")
          .tools(Collections.singletonList(new FunctionTool(ToolUtil.weatherFunction())))
          .temperature(0D)
          .build();
  Assistant assistant = service.createAssistant(assistantRequest);
  String assistantId = assistant.getId();

  //create thread
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

  //wait for the run to complete
  Run retrievedRun = service.retrieveRun(threadId, run.getId());
  while (!(retrievedRun.getStatus().equals("completed"))
          && !(retrievedRun.getStatus().equals("failed"))
          && !(retrievedRun.getStatus().equals("expired"))
          && !(retrievedRun.getStatus().equals("incomplete"))
          && !(retrievedRun.getStatus().equals("requires_action"))) {
    retrievedRun = service.retrieveRun(threadId, run.getId());
  }
  //print the result
  System.out.println(retrievedRun);

  RequiredAction requiredAction = retrievedRun.getRequiredAction();
  List<ToolCall> toolCalls = requiredAction.getSubmitToolOutputs().getToolCalls();
  ToolCall toolCall = toolCalls.get(0);
  ToolCallFunction function = toolCall.getFunction();
  String toolCallId = toolCall.getId();

  //submit tool output with get_weather function
  SubmitToolOutputsRequest submitToolOutputsRequest = SubmitToolOutputsRequest.ofSingletonToolOutput(toolCallId, executor.executeAndConvertToJson(function).toPrettyString());
  retrievedRun = service.submitToolOutputs(threadId, retrievedRun.getId(), submitToolOutputsRequest);

  while (!(retrievedRun.getStatus().equals("completed"))
          && !(retrievedRun.getStatus().equals("failed"))
          && !(retrievedRun.getStatus().equals("expired"))
          && !(retrievedRun.getStatus().equals("incomplete"))
          && !(retrievedRun.getStatus().equals("requires_action"))) {
    retrievedRun = service.retrieveRun(threadId, run.getId());
  }

  //print the result with tool call
  System.out.println(retrievedRun);

  //get result message list
  OpenAiResponse<Message> response = service.listMessages(threadId, new ListSearchParameters());
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

# License
Released under the MIT License


