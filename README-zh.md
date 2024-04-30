![Maven Central](https://img.shields.io/maven-central/v/io.github.lambdua/service?color=blue)

# OpenAi4J

中文文档不是最新的,目前没有时间精力去维护,如果你有兴趣,可以提交PR,我会尽快合并,建议查看[英文文档](README.md)!</br>
中文文档不是最新的,目前没有时间精力去维护,如果你有兴趣,可以提交PR,我会尽快合并,建议查看[英文文档](README.md)!</br>
中文文档不是最新的,目前没有时间精力去维护,如果你有兴趣,可以提交PR,我会尽快合并,建议查看[英文文档](README.md)!</br>

> ⚠️ 这个项目是[openai Java](https://github.com/TheoKanning/openai-java)
> 项目的分叉,原项目作者似乎已经停止维护,无法满足我的需求，所以我决定继续维护这个项目,并添加新功能。
> [版本变化详情](https://github.com/Lambdua/openai4j/releases)


用于使用OpenAI的GPT API的Java库。支持openAi所有的模型,支持最新的gpt4-trubo识图模型

项目结构:

- `api` : GPT API的请求/响应POJO对象,用于与OpenAI API交互。
- `client` : 一个基本的Retrofit客户端,用于GPT终端,包含`api`模块。
- `service` : 一个基本的服务类,用于创建和调用客户端。这是接入openai的java最简单的方式。
- `example` : 一些示例代码,用于展示如何使用这个库。

## 支持的API

[模型](https://platform.openai.com/docs/api-reference/models)  [补全](https://platform.openai.com/docs/api-reference/completions) [聊天](https://platform.openai.com/docs/api-reference/chat/create) [编辑](https://platform.openai.com/docs/api-reference/edits) [嵌入](https://platform.openai.com/docs/api-reference/embeddings) [音频](https://platform.openai.com/docs/api-reference/audio) [文件](https://platform.openai.com/docs/api-reference/files) [微调](https://platform.openai.com/docs/api-reference/fine-tuning) [图像](https://platform.openai.com/docs/api-reference/images) [审核](https://platform.openai.com/docs/api-reference/moderations) [助手](https://platform.openai.com/docs/api-reference/assistants)

# 快速开始

## 导入

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

如果你只是单纯的想使用pojo,你可以导入api模块

```xml

<dependency>
    <groupId>io.github.lambdua</groupId>
    <artifactId>api</artifactId>
    <version>0.20.0</version>
</dependency>
```

## OpenAiService 使用

如果您正在寻找最快的解决方案，请导入 service 模块并使用 OpenAiService。

```java
OpenAiService openAiService = new OpenAiService(API_KEY);
//开始流式对话
List<ChatMessage> messages = new ArrayList<>();
ChatMessage systemMessage = new SystemMessage("You are a dog and will speak as such.");
messages.

add(systemMessage);

ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
        .model("gpt-3.5-turbo")
        .messages(messages)
        .n(1)
        .maxTokens(50)
        .build();
service.

streamChatCompletion(chatCompletionRequest).

blockingForEach(System.out::println);
```

## 自定义OpenAiService

openAiService支持多种方式创建,你可以参考example包中的`example.ServiceCreateExample`示例

```java
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

## gpt-4-turbo/gpt-vision 识图支持

```java
        final List<ChatMessage> messages = new ArrayList<>();
final ChatMessage systemMessage = new SystemMessage("You are a helpful assistant.");
//这里的imageMessage是一个识图消息
final ChatMessage imageMessage = UserMessage.buildImageMessage("What'\''s in this image?",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg");
        messages.

add(systemMessage);
        messages.

add(imageMessage);

ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
        .builder()
        .model("gpt-4-turbo")
        .messages(messages)
        .n(1)
        .maxTokens(200)
        .build();

ChatCompletionChoice choice = service.createChatCompletion(chatCompletionRequest).getChoices().get(0);
        System.out.

println(choice.getText());
```

## Tools or Functions

本库支持已经过时的function调用,同时支持最新的tool调用.
首先我们声明函数参数:

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

接下来,我们声明函数本身并将其与一个执行器相关联,在本例中,我们将模拟来自某个API的响应:

```java
//首先声明一个function,获取天气
ChatFunction function = ChatFunction.builder()
        .name("get_weather")
        .description("Get the current weather in a given location")
        //这里的executor是一个lambda表达式,这个lambda表达式接受一个Weather对象,返回一个WeatherResponse对象
        .executor(Weather.class, w -> new WeatherResponse(w.location, w.unit, 25, "sunny"))
        .build();
```

## Tools

然后使用service进行chatCompletion请求,并传入tool

```java
        //声明一个工具,目前openai-tool只支持function类型的tool
final ChatTool tool = new ChatTool(function);
final List<ChatMessage> messages = new ArrayList<>();
final ChatMessage systemMessage = new SystemMessage("You are a helpful assistant.");
final ChatMessage userMessage = new UserMessage("What is the weather in Monterrey, Nuevo León?");
messages.

add(systemMessage);
messages.

add(userMessage);
ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
        .builder()
        .model("gpt-3.5-turbo-0613")
        .messages(messages)
        //这里的tools是一个list,可以传入多个tool
        .tools(Arrays.asList(tool))
        .toolChoice("auto")
        .n(1)
        .maxTokens(100)
        .build();
//发送请求
ChatCompletionChoice choice = service.createChatCompletion(chatCompletionRequest).getChoices().get(0);
```

你还可以使用名为FunctionExecutor的执行器轻松处理函数。

```java
        FunctionExecutor toolExecutor = new FunctionExecutor(Arrays.asList(function));
Object functionExecutionResponse = toolExecutor.execute(toolCall.getFunction());
```

> 更多有关于tool的使用,可以参考我们的测试用例`ChatCompletionTest`。

### Functions

functions已经是过时的调用方式,不过我们仍然支持它,你可以参考example包中找到更多示例,也可以查看我们的测试用例`ChatCompletionTest`.

> **Note:** 注意: FunctionExecutor类是'service'模块的一部分。

您还可以创建自己的函数执行器。ChatFunctionCall.getArguments()的返回对象是JsonNode,出于简单性考虑,它应该可以帮助您实现这一点。

要深入了解,请参考: OpenAiApiFunctionsExample.java 中使用函数的对话示例。 或者使用函数和流的示例:
OpenAiApiFunctionsWithStreamExample.java

### 流线程关闭

如果你想在流响应后立即关闭你的进程,请调用OpenAiService.shutdownExecutor()。
对于非流调用,这是不必要的。

## 仅使用pojo

如果您想要创建自己的客户端，只需从 api 模块导入 POJOs。
您的客户端需要使用蛇形命名来与 OpenAI API 协作。

## Retrofit 客户端

如果您正在使用 retrofit，可以导入 client 模块并使用 OpenAiApi。
您需要添加您的身份验证令牌作为头部（参见 AuthenticationInterceptor）
并设置您的转换器工厂以使用蛇形命名并仅包含非空字段。

## 常见问题

### 支持自定义openai-url或代理url吗?

是的,你可以在OpenAiService构造函数中传递一个url,它将被用作基础url。

```java
OpenAiService service = new OpenAiService("你的令牌", "baseUrl或者代理url");
```

### 这支持函数吗?

支持,使用自己的函数而不必担心做脏活累活是非常容易的。你可以参考OpenAiApiFunctionsExample.java或OpenAiApiFunctionsWithStreamExample.java项目中的示例。

### 为什么我会遇到连接超时?

请确认你的网络连接是稳定的,并且你的OpenAI服务器是可用的。如果你的网络连接不稳定,你可以尝试增加超时时间。

## 许可证

按MIT许可证发布
