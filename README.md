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

[Models](https://platform.openai.com/docs/api-reference/models) , [Completions](https://platform.openai.com/docs/api-reference/completions) ,[Assistants-v1](https://platform.openai.com/docs/api-reference/assistants-v1), [Chat](https://platform.openai.com/docs/api-reference/chat/create) , [Edits](https://platform.openai.com/docs/api-reference/edits) , [Embeddings](https://platform.openai.com/docs/api-reference/embeddings) , [Audio](https://platform.openai.com/docs/api-reference/audio) , [Files](https://platform.openai.com/docs/api-reference/files) , [Fine-tuning](https://platform.openai.com/docs/api-reference/fine-tuning) , [Images](https://platform.openai.com/docs/api-reference/images) , [Moderations](https://platform.openai.com/docs/api-reference/moderations)

# Roadmap

- [Assistants-v2](https://platform.openai.com/docs/api-reference/assistants),assistant-v1 will be removed in version
  0.20.0

# Quick Start

## Import
### Gradle
`implementation 'io.github.lambdua:<api|client|service>:0.19.1'`
### Maven
```xml
   <dependency>
    <groupId>io.github.lambdua</groupId>
    <artifactId>service</artifactId>
    <version>0.19.1</version>       
   </dependency>
```

To utilize pojos, import the api module:

```xml
   <dependency>
    <groupId>io.github.lambdua</groupId>
    <artifactId>api</artifactId>
    <version>0.19.1</version>       
   </dependency>
```

## Using OpenAiService

For a rapid deployment, import the service module and deploy OpenAiService.

```java
//api-key get from environment variable OPENAI_API_KEY
OpenAiService openAiService = new OpenAiService();
//Initiate a streaming conversation
List<ChatMessage> messages = new ArrayList<>();
ChatMessage systemMessage = a SystemMessage("You are a dog and will speak as such.");
messages.add(systemMessage);
ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
        .model("gpt-3.5-turbo")
        .messages(messages)
        .n(1)
        .maxTokens(50)
        .build();
service.streamChatCompletion(chatCompletionRequest).blockingForEach(System.out::println);
```

## Customizing OpenAiService

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

## gpt-4-turbo/gpt-vision Image Recognition Support

```java
final List<ChatMessage> messages = an ArrayList<>();
final ChatMessage systemMessage = a SystemMessage("You are a helpful assistant.");
//Here, the imageMessage is intended for image recognition
final ChatMessage imageMessage = UserMessage.buildImageMessage("What's in this image?",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg");
        messages.add(systemMessage);
        messages.add(imageMessage);
ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
        .model("gpt-4-turbo")
        .messages(messages)
        .n(1)
        .maxTokens 200)
        .build();
ChatCompletionChoice choice = service.createChatCompletion(chatCompletionRequest).getChoices().get(0);
        System.out.println(choice.getText());

```

## Tools or Functions

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

## Tools

Then, the service is used for a chatCompletion request, incorporating the tool:

```java
//A tool is declared; currently, openai-tool only supports the function type.
final ChatTool tool = new ChatTool(function);
final List<ChatMessage> messages = an ArrayList<>();
final ChatMessage systemMessage = a SystemMessage("You are a helpful assistant.");
final ChatMessage userMessage = a UserMessage("What is the weather in Monterrey, Nuevo León?");
        messages.add(systemMessage);
        messages.add(userMessage);

ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
        .model("gpt-3.5-turbo-0613")
        .messages(messages)
        //Tools is a list; multiple tools can be included
        .tools(Arrays.asList(tool))
        .toolChoice("auto")
        .n(1)
        .maxTokens 100)
        .build();
//Request is sent
ChatCompletionChoice choice = service.createChatCompletion(chatCompletionRequest).getChoices().get(0);
```

You can also employ a FunctionExecutor to handle functions with ease:

```java
        FunctionExecutor toolExecutor = new FunctionExecutor(Arrays.asList(function));
        Object functionExecutionResponse = toolExecutor.execute(toolCall.getFunction());
```

> For further information on using tools, refer to our test case ChatCompletionTest.

### Functions

Although function calling is an outdated approach, it remains supported. For more examples, consult the example package
or our test case `ChatCompletionTest`.

> **Note:** The FunctionExecutor class is a component of the 'service' module.

You can create your own function executor. Since ChatFunctionCall.getArguments() returns a JsonNode, it simplifies the
implementation process.
For more detailed information, refer to using functions in dialogue examples in OpenAiApiFunctionsExample.java or
streaming functions in OpenAiApiFunctionsWithStreamExample.java.

### Stream Thread Shutdown

To immediately terminate your process following a stream response, invoke OpenAiService.shutdownExecutor().
This action is unnecessary for non-stream calls.

## Just Using POJO

If you wish to develop your own client, simply import POJOs from the api module.
Ensure your client adopts snake case naming for compatibility with the OpenAI API.

## Retrofit Client

For those utilizing Retrofit, the client module can be imported for use with OpenAiApi.
Your authentication token must be added as a header (see AuthenticationInterceptor), and your converter factory should
be configured to use snake case naming and exclude null fields.

## FAQs

### Is it possible to customize the OpenAI URL or use a proxy URL?

Yes, you can specify a URL when constructing OpenAiService, which will serve as the base URL.
```java
OpenAiService service = new OpenAiService("your token", "baseUrl or proxy url");
```

### Why am I experiencing connection timeouts?

Ensure your network is stable and your OpenAI server is accessible.
If you face network instability, consider increasing the timeout duration.

## License

Released under the MIT License


