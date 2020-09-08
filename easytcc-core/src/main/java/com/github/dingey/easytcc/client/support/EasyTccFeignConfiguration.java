package com.github.dingey.easytcc.client.support;

import com.github.dingey.easytcc.Const;
import com.github.dingey.easytcc.client.EasyTccContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnClass(RequestInterceptor.class)
@Configuration
public class EasyTccFeignConfiguration {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * 创建Feign请求拦截器，在发送请求前设置认证的token,各个微服务将token设置到环境变量中来达到通用
     */
    @Bean
    public EasyTccFeignRequestInterceptor easyTccFeignRequestInterceptor() {
        if (log.isInfoEnabled()) {
            log.info("Initializing EasyTcc Feign client support");
        }
        return new EasyTccFeignRequestInterceptor();
    }

    class EasyTccFeignRequestInterceptor implements RequestInterceptor {
        protected transient Log logger = LogFactory.getLog(this.getClass());

        @Override
        public void apply(RequestTemplate requestTemplate) {
            logger.debug("判断是否传递sirius事务ID");
            if (EasyTccContext.isTransation()) {
                logger.debug("传递sirius事务ID" + EasyTccContext.getGroupId());
                requestTemplate.header(Const.SIRIUS_PREFIX, EasyTccContext.getGroupId());
            }
        }
    }
}
