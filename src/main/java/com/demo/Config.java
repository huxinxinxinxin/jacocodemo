package com.demo;

import co.paralleluniverse.fibers.httpclient.FiberHttpClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class Config extends WebMvcConfigurationSupport {

    @Autowired
    private HttpClient httpClient;

    @Bean
    public HttpClient fiberHttpClient() {
        return FiberHttpClientBuilder.
                create(10).
                setMaxConnPerRoute(1000).
                setMaxConnTotal(10000).build();
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectionRequestTimeout(50000);
        factory.setReadTimeout(50000);
        // 构造加入interceptor
        List<ClientHttpRequestInterceptor> interceptorList = new ArrayList<>();
        interceptorList.add(new ScClientHttpRequestInterceptor());
        InterceptingClientHttpRequestFactory interceptorFactory = new InterceptingClientHttpRequestFactory(new BufferingClientHttpRequestFactory(factory), interceptorList);
        restTemplate.setRequestFactory(interceptorFactory);
        return restTemplate;
    }

    @Override
    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
        jacksonConverter.setObjectMapper(new ObjectMapper());
        converters.add(jacksonConverter);
    }

}
