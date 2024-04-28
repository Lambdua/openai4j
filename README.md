![Maven Central](https://img.shields.io/maven-central/v/io.github.lambdua/service?color=blue)
> That it’s an unofficial library.</br>
> ⚠️ This project is a fork of the [openai Java project](https://github.com/TheoKanning/openai-java). </br>
> The original author appears to have ceased maintenance, failing to meet my needs, prompting me to continue its
> development and adapting to the new features of OpenAI API.


[中文文档-不是最新☕](README-zh.md)

# OpenAI-Java

A Java library for utilizing the GPT API from OpenAI. It supports all OpenAI models, including the latest gpt4-turbo
vision model.

Project structure:
- api: Objects for GPT API request/response handling, facilitating interaction with the OpenAI API.
- client: A basic Retrofit client designed for GPT endpoints, including the api module.
- service: A fundamental service class for creating and invoking the client, offering the most straightforward approach
  to integrating OpenAI in Java.
- example: Sample code demonstrating the library's utilization. 

# Supported APIs

[Models](https://platform.openai.com/docs/api-reference/models) , [Completions](https://platform.openai.com/docs/api-reference/completions) ,[~~Assistants-v1~~](https://platform.openai.com/docs/api-reference/assistants-v1), [Chat](https://platform.openai.com/docs/api-reference/chat/create) , [Edits](https://platform.openai.com/docs/api-reference/edits) , [Embeddings](https://platform.openai.com/docs/api-reference/embeddings) , [Audio](https://platform.openai.com/docs/api-reference/audio) , [Files](https://platform.openai.com/docs/api-reference/files) , [Fine-tuning](https://platform.openai.com/docs/api-reference/fine-tuning) , [Images](https://platform.openai.com/docs/api-reference/images) , [Moderations](https://platform.openai.com/docs/api-reference/moderations)

# v0.20.0 will released feature

- [x] assistant-v2-pojo
- [x] assistant-v2-client
- [x] assistant-v2-service
- [ ] assistant-v2-streaming
- [x] assistant-v2-tool
- [x] assistant-v1 removed in 0.20.0
- [x] batch api pojo
- [x] batch api client
- [x] batch api service

# Quick Start

## Import
### Gradle

`implementation 'io.github.lambdua:<api|client|service>:0.20.0'`
### Maven

<details>
<summary>Maven</summary>

```xml
   <dependency>
    <groupId>io.github.lambdua</groupId>
    <artifactId>service</artifactId>
    <version>0.20.0</version>       
   </dependency>
```
To utilize pojos, import the api module:

```xml
   <dependency>
    <groupId>io.github.lambdua</groupId>
    <artifactId>api</artifactId>
    <version>0.20.0</version>       
   </dependency>
```

</details>

## Using OpenAiService
For a rapid deployment, import the service module and deploy OpenAiService.
```java
void a() {
    //api-key get from environment variable OPENAI_API_KEY
    OpenAiService openAiService = new OpenAiService();
    //Initiate a streaming conversation
    List<ChatMessage> messages = new ArrayList<>();
    ChatMessage systemMessage = new SystemMessage("You are a dog and will speak as such.");
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

## gpt-4-turbo/gpt-vision Image Recognition Support

```java
void a(){
final List<ChatMessage> messages = new ArrayList<>();
final ChatMessage systemMessage = new  SystemMessage("You are a helpful assistant.");
//Here, the imageMessage is intended for image recognition
final ChatMessage imageMessage = UserMessage.buildImageMessage("What's in this image?",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg");
messages.add(systemMessage);
messages.add(imageMessage);

ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
        .model("gpt-4-turbo")
        .messages(messages)
        .n(1)
        .maxTokens (200)
        .build();
ChatCompletionChoice choice = service.createChatCompletion(chatCompletionRequest).getChoices().get(0);
System.out.println(choice.getText());
}
```

# Just Using POJO

If you wish to develop your own client, simply import POJOs from the api module.</br>
Ensure your client adopts snake case naming for compatibility with the OpenAI API.

# other examples:
The sample code is all in the `example` package, which includes most of the functional usage. </br>
You can refer to the code in the example package. Below are some commonly used feature usage examples
<details>
<summary>Customizing OpenAiService</summary>
OpenAiService is versatile in its setup options, as demonstrated in the `example.ServiceCreateExample` within the
example package.

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
//A tool is declared; currently, openai-tool only supports the function type.
void a(){
final ChatTool tool = new ChatTool(function);
final List<ChatMessage> messages = new  ArrayList<>();
final ChatMessage systemMessage = new  SystemMessage("You are a helpful assistant.");
final ChatMessage userMessage = new UserMessage("What is the weather in Monterrey, Nuevo León?");
messages.add(systemMessage);
messages.add(userMessage);

ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
        .model("gpt-3.5-turbo-0613")
        .messages(messages)
        //Tools is a list; multiple tools can be included
        .tools(Arrays.asList(tool))
        .toolChoice("auto")
        .n(1)
        .maxTokens(100)
        .build();
//Request is sent
ChatCompletionChoice choice = service.createChatCompletion(chatCompletionRequest).getChoices().get(0);
}
```

</details>
<details>
<summary>function(deprecated)</summary>

```java
class OpenAiApiFunctionsExample {

  @JsonSchemaDescription("Get the current weather of a location")
  public static class Weather {
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

    public WeatherResponse(String location, WeatherUnit unit, int temperature, String description) {
      this.location = location;
      this.unit = unit;
      this.temperature = temperature;
      this.description = description;
    }
  }

  public static void main(String... args) {
    OpenAiService service = new OpenAiService();

    FunctionExecutor functionExecutor = new FunctionExecutor(Collections.singletonList(ChatFunction.builder()
            .name("get_weather")
            .description("Get the current weather of a location")
            .executor(Weather.class, w -> new WeatherResponse(w.location, w.unit, new Random().nextInt(50), "sunny"))
            .build()));


    List<ChatMessage> messages = new ArrayList<>();
    ChatMessage systemMessage = new SystemMessage("You are an assistant that answers using the local slang of the given place, uncensored.");
    messages.add(systemMessage);

    System.out.print("First Query: ");
    Scanner scanner = new Scanner(System.in);
    ChatMessage firstMsg = new UserMessage(scanner.nextLine());
    messages.add(firstMsg);

    while (true) {
      ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
              .builder()
              .model("gpt-3.5-turbo-0613")
              .messages(messages)
              .functions(functionExecutor.getFunctions())
              .functionCall(ChatCompletionRequestFunctionCall.of("auto"))
              .n(1)
              .maxTokens(100)
              .logitBias(new HashMap<>())
              .build();
      AssistantMessage responseMessage = service.createChatCompletion(chatCompletionRequest).getChoices().get(0).getMessage();
      messages.add(responseMessage); // don't forget to update the conversation with the latest response

      ChatFunctionCall functionCall = responseMessage.getFunctionCall();
      if (functionCall != null) {
        System.out.println("Trying to execute " + functionCall.getName() + "...");
        Optional<FunctionMessage> message = functionExecutor.executeAndConvertToMessageSafely(functionCall);
                /* You can also try 'executeAndConvertToMessage' inside a try-catch block, and add the following line inside the catch:
                "message = executor.handleException(exception);"
                The content of the message will be the exception itself, so the flow of the conversation will not be interrupted, and you will still be able to log the issue. */

        if (message.isPresent()) {
                    /* At this point:
                    1. The function requested was found
                    2. The request was converted to its specified object for execution (Weather.class in this case)
                    3. It was executed
                    4. The response was finally converted to a ChatMessage object. */

          System.out.println("Executed " + functionCall.getName() + ".");
          messages.add(message.get());
          continue;
        } else {
          System.out.println("Something went wrong with the execution of " + functionCall.getName() + "...");
          break;
        }
      }

      System.out.println("Response: " + responseMessage.getContent());
      System.out.print("Next Query: ");
      String nextLine = scanner.nextLine();
      if (nextLine.equalsIgnoreCase("exit")) {
        System.exit(0);
      }
      messages.add(new UserMessage(nextLine));
    }
  }

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
  messages.add(new UserMessage(new ImageContent("text", "textContent", new ImageUrl("dddd"))));
  int tokens_1 = TikTokensUtil.tokens(TikTokensUtil.ModelEnum.GPT_3_5_TURBO.getName(), messages);
  int tokens_2 = TikTokensUtil.tokens(TikTokensUtil.ModelEnum.GPT_3_5_TURBO.getName(), "Hello OpenAI 1.");
  int tokens_3 = TikTokensUtil.tokens(TikTokensUtil.ModelEnum.GPT_4_TURBO.getName(), messages);
}
```

</details>

# FAQs

# Is it possible to customize the OpenAI URL or use a proxy URL?

Yes, you can specify a URL when constructing OpenAiService, which will serve as the base URL.But we recommend using the
environment variable OPENAI_API_BASE_URL and OPENAI_API_KEY to load the OpenAI API key.
```java
OpenAiService service = new OpenAiService("your token", "baseUrl or proxy url");
```

## Why am I experiencing connection timeouts?

Ensure your network is stable and your OpenAI server is accessible.
If you face network instability, consider increasing the timeout duration.

# License

Released under the MIT License


