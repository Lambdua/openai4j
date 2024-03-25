![Maven Central](https://img.shields.io/maven-central/v/io.github.lambdua/service?color=blue)

> ⚠️ 这个项目是[openai Java](https://github.com/TheoKanning/openai-java)项目的分叉,原项目作者似乎已经停止维护,无法满足我的需求，所以我决定继续维护这个项目,并添加新功能。
> [版本变化详情](https://github.com/Lambdua/openai4j/releases)

[english doc](README-EN.md)
# OpenAI-Java
用于使用OpenAI的GPT API的Java库。支持GPT-3、ChatGPT和GPT-4。

包括以下工件:
- `api` : GPT API的请求/响应POJO。
- `client` : 一个基本的Retrofit客户端,用于GPT终端,包含`api`模块。
- `service` : 一个基本的服务类,用于创建和调用客户端。这是开始使用的最简单方式。

以及使用service的示例项目。

## 支持的API
- [模型](https://platform.openai.com/docs/api-reference/models)
- [补全](https://platform.openai.com/docs/api-reference/completions)
- [聊天](https://platform.openai.com/docs/api-reference/chat/create)
- [编辑](https://platform.openai.com/docs/api-reference/edits)
- [嵌入](https://platform.openai.com/docs/api-reference/embeddings)
- [音频](https://platform.openai.com/docs/api-reference/audio)
- [文件](https://platform.openai.com/docs/api-reference/files)
- [微调](https://platform.openai.com/docs/api-reference/fine-tuning)
- [图像](https://platform.openai.com/docs/api-reference/images)
- [审核](https://platform.openai.com/docs/api-reference/moderations)
- [助手](https://platform.openai.com/docs/api-reference/assistants)

#### 已被OpenAI弃用
- [Engines](https://platform.openai.com/docs/api-reference/engines)
- [旧版微调](https://platform.openai.com/docs/guides/legacy-fine-tuning)

## 导入

### Gradle
`implementation 'io.github.lambdua:<api|client|service>:<version>'`

### Maven
```xml
   <dependency>
    <groupId>io.github.lambdua</groupId>
    <artifactId>{api|client|service}</artifactId>
    <version>0.18.4</version>       
   </dependency>
```

# 使用方法
## OpenAiService
如果您正在寻找最快的解决方案，请导入 service 模块并使用 OpenAiService。

> ⚠️ client模块中的OpenAiService已被弃用,请切换到service模块中的新版本,将在0.19版本中删除,请尽快切换。
> 更多使用方法请参考example包下的示例代码。

```java
OpenAiService service = new OpenAiService("你的令牌","baseUrl或者代理url");
CompletionRequest completionRequest = CompletionRequest.builder()
        .prompt("曾经有人告诉我这个世界会离开我")
        .model("babbage-002")
        .echo(true)
        .build();
service.createCompletion(completionRequest).getChoices().forEach(System.out::println);
```

### 自定义OpenAiService
如果你需要自定义OpenAiService,创建你自己的Retrofit客户端并将其传递到构造函数中。
例如,按照以下步骤添加请求日志记录(在添加日志记录gradle依赖项之后):

```java
ObjectMapper mapper = defaultObjectMapper();
OkHttpClient client = defaultClient(token, timeout)
        .newBuilder()
        .interceptor(HttpLoggingInterceptor())
        .build();
Retrofit retrofit = defaultRetrofit(client, mapper);

OpenAiApi api = retrofit.create(OpenAiApi.class);
OpenAiService service = new OpenAiService(api);

```
### Functions
您可以使用ChatFunction类轻松创建您的函数并定义它们的执行器,以及任何将用于定义其可用参数的自定义类。您还可以使用名为FunctionExecutor的执行器轻松处理函数。
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
ChatFunction.builder()
        .name("get_weather")
        .description("Get the current weather of a location")
        .executor(Weather.class, w -> new WeatherResponse(w.location, w.unit, new Random().nextInt(50), "sunny"))
        .build();
```

然后,我们使用'service'模块中的FunctionExecutor对象来协助执行和转换为一个准备好进行对话的对象:
```java
List<ChatFunction> functionList = // list with functions
FunctionExecutor functionExecutor = new FunctionExecutor(functionList);

List<ChatMessage> messages = new ArrayList<>();
ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), "Tell me the weather in Barcelona.");
messages.add(userMessage);
ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
        .builder()
        .model("gpt-3.5-turbo-0613")
        .messages(messages)
        .functions(functionExecutor.getFunctions())
        .functionCall(new ChatCompletionRequestFunctionCall("auto"))
        .maxTokens(256)
        .build();

ChatMessage responseMessage = service.createChatCompletion(chatCompletionRequest).getChoices().get(0).getMessage();
ChatFunctionCall functionCall = responseMessage.getFunctionCall(); // might be null, but in this case it is certainly a call to our 'get_weather' function.

ChatMessage functionResponseMessage = functionExecutor.executeAndConvertToMessageHandlingExceptions(functionCall);
messages.add(response);
```
> **Note:** 注意: FunctionExecutor类是'service'模块的一部分。

您还可以创建自己的函数执行器。ChatFunctionCall.getArguments()的返回对象是JsonNode,出于简单性考虑,它应该可以帮助您实现这一点。

要深入了解,请参考: OpenAiApiFunctionsExample.java 中使用函数的对话示例。 或者使用函数和流的示例: OpenAiApiFunctionsWithStreamExample.java

### 流线程关闭
如果你想在流响应后立即关闭你的进程,请调用OpenAiService.shutdownExecutor()。
对于非流调用,这是不必要的。

## 仅数据类

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
OpenAiService service = new OpenAiService("你的令牌","baseUrl或者代理url");
```

### 这支持函数吗?
当然!使用自己的函数而不必担心做脏活累活是非常容易的。你可以参考OpenAiApiFunctionsExample.java或OpenAiApiFunctionsWithStreamExample.java项目中的示例。

### 为什么我会遇到连接超时?
请确认你的网络连接是稳定的,并且你的OpenAI服务器是可用的。如果你的网络连接不稳定,你可以尝试增加超时时间。

## 许可证
按MIT许可证发布




