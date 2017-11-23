package com.demo;

import com.google.common.io.CharStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.InputStreamReader;

public class ScClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScClientHttpRequestInterceptor.class);
    private static final int WARN_TIME = 10000;
    private static final int ERROR_TIME = 30000;

    public ScClientHttpRequestInterceptor() {
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException{
        String urlString = cutdownBaiduUrlLength(request);
        LOGGER.info("Request {} ", urlString);
        LOGGER.info("Request headers {}", request.getHeaders().toSingleValueMap());
        long start = System.currentTimeMillis();
        IOException ioException = null;
        ClientHttpResponse response = null;
        try {
            response = execution.execute(request, body);
        } catch (IOException e) {
            ioException = e;
        } catch (Exception e) {
            ioException = new IOException("wrapped IOException", e);
        }

        String responseBody = null;
        if (response != null) {
            LOGGER.info("Response status: {}", response.getStatusCode());
            LOGGER.info("Response content type: {}", response.getHeaders().getContentType());
            if (response.getHeaders().getContentType() != null) {
                if ((response.getHeaders().getContentType().includes(MediaType.APPLICATION_JSON) ||
                        response.getHeaders().getContentType().includes(MediaType.TEXT_PLAIN) ||
                        response.getHeaders().getContentType().includes(MediaType.APPLICATION_XML) ||
                        response.getHeaders().getContentType().includes(MediaType.APPLICATION_OCTET_STREAM) ||
                        response.getHeaders().getContentType().includes(MediaType.TEXT_HTML))) {
                    responseBody = CharStreams.toString(new InputStreamReader(response.getBody(), "UTF-8"));
                    String str = responseBody;
                    if (str.length() > 200) {
                        str = str.substring(0, 200);
                    }
                    LOGGER.info("Response body: {}", str);
                }
            }
        }

        long usedTime = System.currentTimeMillis() - start;
        String str = "\n--------------------------------\n" +
                "{}\n" +
                "request -> response usedTime {} \n" +
                "--------------------------------\n";
        if (usedTime > ERROR_TIME) {
            LOGGER.error("Too slow, Need optimize" + str, urlString, usedTime);
        } else if (usedTime > WARN_TIME) {
            LOGGER.warn("Need optimize" + str, urlString, usedTime);
        } else {
            LOGGER.info(str, urlString, usedTime);
        }
        if (ioException != null) {
            throw ioException;
        }
        return response;
    }

    private String cutdownBaiduUrlLength(HttpRequest request) {
        if (request.getURI().getHost().equals("api.fanyi.baidu.com")) {
            return "http://api.fanyi.baidu.com" + request.getURI().getPath();
        } else {
            return request.getMethod().toString() + " " + request.getURI().toString();
        }
    }

}
