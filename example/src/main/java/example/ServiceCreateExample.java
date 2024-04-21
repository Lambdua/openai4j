package example;

import com.theokanning.openai.client.OpenAiApi;
import com.theokanning.openai.service.OpenAiService;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import retrofit2.Retrofit;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author LiangTao
 * @date 2024年04月12 15:00
 **/
public class ServiceCreateExample {

    private static final String BASE_URL = "https://api.openai.com/v1/";
    private static final String API_KEY = "sk-1234567890";

    public static void main(String[] args) {
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
    }
}
