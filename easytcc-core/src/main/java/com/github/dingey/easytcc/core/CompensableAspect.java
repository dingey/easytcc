package com.github.dingey.easytcc.core;

import com.github.dingey.easytcc.EasytccProperties;
import com.github.dingey.easytcc.client.EasyTccContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import java.util.Objects;

@Component
@Aspect
@Order(1)
public class CompensableAspect {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Resource
    private CompensableExecutorService service;
    @Resource
    private EasytccProperties properties;
    @Resource
    private ApplicationContext context;
    @Value("${spring.application.name}")
    String appName;

    @PostConstruct
    public void init() {
        if (log.isInfoEnabled()) {
            log.info("Initializing EasyTcc Compensable Aspect");
        }
    }

    @Pointcut(value = "@annotation(compensable)", argNames = "compensable")
    public void pointcut(Compensable compensable) {
    }

    @Around(value = "pointcut(compensable)", argNames = "pjp,compensable")
    public Object around(ProceedingJoinPoint pjp, Compensable compensable) throws Throwable {
        if (EasyTccContext.isTransation()) {
            Group joinGroup = Group.joinGroup(EasyTccContext.getGroupId(), EasyTccContext.getXid());
            return proceedWithTcc(pjp, joinGroup);
        } else {
            Group group = Group.createGroup();

            EasyTccContext.setGroup(group.getGroupId());
            EasyTccContext.setXid(group.getXid());
            return proceedWithTcc(pjp, group);
        }
    }

    private Object proceedWithTcc(ProceedingJoinPoint pjp, Group group) throws Throwable {
        try {
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            log.debug("TCC事务trying_start group: {} 分支xid :{} 参数：{}", EasyTccContext.getGroupId(), EasyTccContext.getXid(), service.toJson(pjp.getArgs()));
            String sync = sendSync(group);
            if (!Objects.equals(sync, "1")) {
                throw new RuntimeException("trying_start fail");
            }
            Object proceed = pjp.proceed();
            log.debug("TCC事务trying_success group: {} 分支xid :{} 参数：{}", EasyTccContext.getGroupId(), EasyTccContext.getXid(), service.toJson(pjp.getArgs()));

            String[] names = context.getBeanNamesForType(signature.getDeclaringType());
            Group.TryingInfo tryingInfo = new Group.TryingInfo()
                    .setBean(names.length > 0 ? names[0] : null)
                    .setMethod(signature.getMethod().getName())
                    .setArgs(pjp.getArgs())
                    .setServiceId(appName);

            group.setStatus(Status.TRYING_SUCCESS.ordinal());
            group.setTrying(tryingInfo);
            send(group);

            EasyTccContext.clear();
            return proceed;
        } catch (Throwable e) {
            log.debug("TCC事务trying_fail group: {} 分支xid :{} 参数：{}", EasyTccContext.getGroupId(), EasyTccContext.getXid(), service.toJson(pjp.getArgs()));
            group.setStatus(Status.TRYING_FAIL.ordinal());
            send(group);
            EasyTccContext.clear();
            throw e;
        }
    }

    private void send(Group group) {
        if (properties.getClient().isAsync()) {
            sendAsync(group);
        } else {
            sendSync(group);
        }
    }

    private String sendSync(Group group) {
        String json = service.toJson(group);
        String response = service.sendSync("http://" + properties.getClient().getServerId() + "/easy/tcc/trying", json);
        log.debug("发送{} 返回{}", json, response);
        return response;
    }

    private void sendAsync(Group group) {
        String json = service.toJson(group);
        service.sendAsync("http://" + properties.getClient().getServerId() + "/easy/tcc/trying", json);
    }
}
