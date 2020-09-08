package com.github.dingey.easytcc.server.handler;

import com.github.dingey.easytcc.EasytccProperties;
import com.github.dingey.easytcc.core.Group;
import com.github.dingey.easytcc.core.CompensableExecutorService;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
public abstract class AbstractHandler implements EasyTccHandler {
    @Resource
    private EasytccProperties properties;
    @Resource
    private CompensableExecutorService service;

    public void send(Group group) {
        if (properties.getServer().isAsync()) {
            sendAsync(group);
        } else {
            sendSync(group);
        }
    }

    public String sendSync(Group group) {
        String json = service.toJson(group);
        String response = service.sendSync("http://" + group.getTrying().getServiceId() + "/easy/tcc/callback", group);
        if (log.isDebugEnabled()) {
            log.debug("服务端发送补偿 -> {}  <- 返回{}", json, response);
        }
        return response;
    }

    public Future<String> sendAsync(Group group) {
        String json = service.toJson(group);
        Future<String> future = service.sendAsync("http://" + group.getTrying().getServiceId() + "/easy/tcc/callback", json);
        if (log.isDebugEnabled()) {
            try {
                log.debug("服务端发送补偿 -> {}  <- 返回{}", json, future.get());
            } catch (InterruptedException | ExecutionException e) {
                log.error(e.getMessage(), e);
            }
        }
        return future;
    }

    public String toJson(Group g) {
        return service.toJson(g);
    }

    public <T> T parseJson(String json, Class<T> type) {
        return service.parseJson(json, type);
    }
}
