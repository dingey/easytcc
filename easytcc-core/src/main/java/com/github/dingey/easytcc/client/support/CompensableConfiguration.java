package com.github.dingey.easytcc.client.support;

import com.github.dingey.easytcc.EasytccProperties;
import com.github.dingey.easytcc.core.CompensableAspect;
import com.github.dingey.easytcc.core.CompensableExecutorService;
import com.github.dingey.easytcc.core.CompensableThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
public class CompensableConfiguration {
    @Resource
    private EasytccProperties properties;

    @Bean
    public CompensableAspect compensableAspect() {
        return new CompensableAspect();
    }

    @Bean
    public CompensableExecutorService compensableExecutorService() {
        return new CompensableExecutorService();
    }

    @Bean
    public ExecutorService easytccExecutor() {
        return new ThreadPoolExecutor(properties.getClient().getThreadPool().getCorePoolSize(),
                properties.getClient().getThreadPool().getMaximumPoolSize(),
                properties.getClient().getThreadPool().getKeepAliveTime(),
                properties.getClient().getThreadPool().getUnit(),
                new LinkedBlockingDeque<>(properties.getClient().getThreadPool().getCapacity()),
                new CompensableThreadFactory("easytcc"), new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
