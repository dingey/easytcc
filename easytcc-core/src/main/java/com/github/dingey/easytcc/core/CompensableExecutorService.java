package com.github.dingey.easytcc.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dingey.easytcc.EasytccProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Slf4j
@Component
@SuppressWarnings("unused")
public class CompensableExecutorService {
    @Resource
    private ExecutorService easytccExecutor;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private EasytccProperties properties;

    public CompensableExecutorService() {
    }

    public CompensableExecutorService(ExecutorService executorService) {
        this.easytccExecutor = executorService;
    }

    public void send(String url, Group group) {
        if (properties.getClient().isAsync()) {
            sendAsync(url, group);
        } else {
            sendSync(url, group);
        }
    }

    public String sendSync(String url, Group group) {
        return send(url, toJson(group));
    }

    public String sendSync(String url, String json) {
        return send(url, json);
    }

    public Future<String> sendAsync(String url, Group group) {
        return easytccExecutor.submit(() -> send(url, toJson(group)));
    }

    public Future<String> sendAsync(String url, String json) {
        return easytccExecutor.submit(() -> send(url, json));
    }

    private String send(String url, String json) {
        String response = postJson(url, json);
        log.debug("发送{} 返回{}", json, response);
        return response;
    }

    private String postJson(String url, String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return restTemplate.postForEntity(url, new HttpEntity<>(json, headers), String.class).getBody();
    }

    public String toJson(Object v) {
        try {
            return objectMapper.writeValueAsString(v);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public <T> T parseJson(String json, Class<T> valueTyp) {
        try {
            return objectMapper.readValue(json, valueTyp);
        } catch (IOException e) {
            return null;
        }
    }
}
