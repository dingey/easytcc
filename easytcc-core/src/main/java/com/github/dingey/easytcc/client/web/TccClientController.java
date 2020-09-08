package com.github.dingey.easytcc.client.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dingey.easytcc.core.Compensable;
import com.github.dingey.easytcc.core.Group;
import com.github.dingey.easytcc.core.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.lang.reflect.Method;

/**
 * @author d
 */
@Slf4j
@RestController
public class TccClientController {
    @Resource
    private ApplicationContext context;
    @Resource
    private ObjectMapper objectMapper;

    @PostMapping(path = "/easy/tcc/callback")
    public Integer callback(@RequestBody Group group) {
        log.debug("执行补偿{}", group);
        Group.TryingInfo trying = group.getTrying();
        if (trying == null || StringUtils.isEmpty(trying.getBean())) {
            return 0;
        }

        Object bean = context.getBean(trying.getBean());
        try {
            Method tryingMethod = getMethod(bean.getClass(), trying.getMethod());
            Compensable compensable = AnnotationUtils.findAnnotation(tryingMethod, Compensable.class);
            if (compensable == null) {
                log.error("找不到对应补偿的注解");
                return 0;
            }
            if (group.getStatus() == Status.CANCEL.ordinal() && StringUtils.hasText(compensable.cancel())) {
                Method cancel = getMethod(bean.getClass(), compensable.cancel());
                cancel.invoke(bean, transform(trying.getArgs(), cancel.getParameterTypes()));
            } else if (group.getStatus() == Status.CONFIRM.ordinal() && StringUtils.hasText(compensable.confirm())) {
                Method confirm = getMethod(bean.getClass(), compensable.confirm());
                confirm.invoke(bean, transform(trying.getArgs(), confirm.getParameterTypes()));
            } else {
                //nothing to do
                log.debug("nothing to do");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return 1;
    }

    private Object[] transform(Object[] args, Class<?>[] types) {
        if (args == null || args.length < 1) {
            return args;
        }
        Object[] os = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            os[i] = objectMapper.convertValue(args[i], types[i]);
        }
        return os;
    }

    private Method getMethod(Class clazz, String name) {
        Method method = ReflectionUtils.findMethod(clazz, name, null);
        if (method != null && !method.isAccessible()) {
            method.setAccessible(true);
        }
        return method;
    }
}
