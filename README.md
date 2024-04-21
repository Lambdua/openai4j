![Maven Central](https://img.shields.io/maven-central/v/io.github.lambdua/service?color=blue)
> ⚠️ This project is a fork of the [openai Java project](https://github.com/TheoKanning/openai-java). The original
> author appears to have ceased maintenance, failing to meet my needs, prompting me to continue its development and
> incorporate new features.
> [Details on version changes](https://github.com/Lambdua/openai4j/releases)


[中文文档☕](README-zh.md)

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

[Models](https://platform.openai.com/docs/api-reference/models) , [Completions](https://platform.openai.com/docs/api-reference/completions) , [Chat](https://platform.openai.com/docs/api-reference/chat/create) , [Edits](https://platform.openai.com/docs/api-reference/edits) , [Embeddings](https://platform.openai.com/docs/api-reference/embeddings) , [Audio](https://platform.openai.com/docs/api-reference/audio) , [Files](https://platform.openai.com/docs/api-reference/files) , [Fine-tuning](https://platform.openai.com/docs/api-reference/fine-tuning) , [Images](https://platform.openai.com/docs/api-reference/images) , [Moderations](https://platform.openai.com/docs/api-reference/moderations) , [Assistants](https://platform.openai.com/docs/api-reference/assistants)

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
OpenAiService openAiService = new OpenAiService(API_KEY);
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
//1. Use the default baseUrl, automatically configured service; initially fetches BaseURL from environment variable (key: OPENAI_API_BASE_URL), defaults to "https://api.openai.com/v1/" if not found.
OpenAiService openAiService = new OpenAiService(API_KEY);
//2. Use a custom baseUrl with the standard configuration
OpenAiService openAiService1 = new OpenAiService(API_KEY, BASE_URL);
//3. Customize the expiration time
OpenAiService openAiService2 = a OpenAiService(API_KEY, Duration.ofSeconds(10));
//4. More advanced customizations
//4.1 Custom okHttpClient
OkHttpClient client = a OkHttpClient.Builder()
// Connection pool
        .connectionPool(new ConnectionPool(Runtime.getRuntime().availableProcessors() * 2, 30, TimeUnit.SECONDS))
        // Custom interceptors for retrying, logging, load balancing, etc.
        // .addInterceptor(new RetryInterceptor())
        // .addInterceptor(new LogInterceptor())
        // .addInterceptor(new Load BalanceInterceptor())
        // Adding a proxy
        // .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxyHost", 8080)))
        .connectTimeout(2, TimeUnit.SECONDS)
        .writeTimeout 3, TimeUnit.SECONDS)
        .readTimeout 10, TimeUnit.SECONDS)
        .protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1))
        .build();
//4.2 Custom Retrofit configuration
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


