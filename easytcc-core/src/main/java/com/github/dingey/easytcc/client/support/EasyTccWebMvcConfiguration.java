package com.github.dingey.easytcc.client.support;

import com.github.dingey.easytcc.Const;
import com.github.dingey.easytcc.client.EasyTccContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
public class EasyTccWebMvcConfiguration implements WebMvcConfigurer {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (log.isInfoEnabled()) {
            log.info("Initializing EasyTcc spring webmvc support");
        }
        registry.addInterceptor(new EasyTccInterceptor());
    }

    public class EasyTccInterceptor implements HandlerInterceptor {
        final Log log = LogFactory.getLog(this.getClass());

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            String groupId = request.getHeader(Const.SIRIUS_PREFIX);
            if (StringUtils.hasText(groupId)) {
                log.debug("tcc事务设置groupId:" + groupId);
                EasyTccContext.setGroup(groupId);
            }
            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
            if (StringUtils.hasText(request.getHeader(Const.SIRIUS_PREFIX))) {
                log.debug("tcc事务清除groupId:" + request.getHeader(Const.SIRIUS_PREFIX));
                EasyTccContext.clear();
            }
        }
    }

}
