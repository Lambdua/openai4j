package com.theokanning.openai.client;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * @author LiangTao
 * @date 2024年05月06 21:31
 **/
public class OrganizationAndProjectHeaderInterceptor implements Interceptor {
    private final String organization;

    private final String project;

    public OrganizationAndProjectHeaderInterceptor(String organization, String project) {
        this.organization = organization;
        this.project = project;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request()
                .newBuilder()
                .header("OpenAI-Organization", organization)
                .header("OpenAI-Project", project)
                .build();
        return chain.proceed(request);
    }
}
