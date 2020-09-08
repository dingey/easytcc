package com.github.dingey.easytcc.server.configuration;

import com.github.dingey.easytcc.EasytccProperties;
import com.github.dingey.easytcc.core.CompensableExecutorService;
import com.github.dingey.easytcc.core.CompensableThreadFactory;
import com.github.dingey.easytcc.server.handler.EasyTccHandler;
import com.github.dingey.easytcc.server.handler.MemoryTccHandler;
import com.github.dingey.easytcc.server.handler.RedisTccHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.concurrent.*;

@Slf4j
@Configuration
public class EasyTccConfiguration {
    @Resource
    private EasytccProperties properties;

    @Bean
    @ConditionalOnProperty(value = "easy.tcc.server.store", havingValue = "none", matchIfMissing = true)
    @ConditionalOnMissingBean
    public EasyTccHandler memoryTccHandler() {
        log.info("Initializing EasyTcc Server memory handler");
        return new MemoryTccHandler();
    }

    @Bean
    @ConditionalOnProperty(value = "easy.tcc.server.store", havingValue = "redis")
    @ConditionalOnClass(name = "org.springframework.data.redis.core.StringRedisTemplate")
    public EasyTccHandler redisTccHandler() {
        log.info("Initializing EasyTcc Server redis handler");
        return new RedisTccHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public CompensableExecutorService compensableExecutorService() {
        return new CompensableExecutorService();
    }

    @Bean
    public ExecutorService easytccExecutor() {
        return new ThreadPoolExecutor(properties.getServer().getThreadPool().getCorePoolSize(),
                properties.getServer().getThreadPool().getMaximumPoolSize(),
                properties.getServer().getThreadPool().getKeepAliveTime(),
                properties.getServer().getThreadPool().getUnit(),
                new LinkedBlockingDeque<>(properties.getServer().getThreadPool().getCapacity()),
                new CompensableThreadFactory("easytcc"), new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
