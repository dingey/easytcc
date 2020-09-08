package com.github.dingey.easytcc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@EnableEasytccServer
@Configuration
@EnableConfigurationProperties(EasytccProperties.class)
@ConditionalOnProperty(value = "easy.tcc.server.enable", havingValue = "true", matchIfMissing = true)
public class EasyTccAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(EasyTccAutoConfiguration.class);

    @PostConstruct
    public void init() {
        log.info("Initializing Easy Tcc Server");
    }
}
