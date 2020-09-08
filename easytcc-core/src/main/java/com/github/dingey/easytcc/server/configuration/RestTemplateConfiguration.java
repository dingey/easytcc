package com.github.dingey.easytcc.server.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@ConditionalOnClass(RestTemplate.class)
@Configuration
public class RestTemplateConfiguration {
    @Bean
    @ConditionalOnMissingBean
    @LoadBalanced
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
