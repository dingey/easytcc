package com.github.dingey.easytcc.client.support;

import com.github.dingey.easytcc.client.EasyTccContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;

import javax.annotation.Resource;
import javax.sql.DataSource;

@ConditionalOnClass(PlatformTransactionManager.class)
@Configuration
public class EasyTccTransactionManagerConfiguration {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Resource
    private DataSource dataSource;

    @Bean
    @ConditionalOnMissingBean
    public DataSourceTransactionManager easyTccTransactionManager() {
        if (log.isInfoEnabled()) {
            log.info("Initializing EasyTcc transation manager");
        }
        return new EasyTccTransactionManager(dataSource);
    }

    class EasyTccTransactionManager extends DataSourceTransactionManager {
        private final Logger log = LoggerFactory.getLogger(this.getClass());

        EasyTccTransactionManager(DataSource dataSource) {
            super(dataSource);
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
            if (EasyTccContext.isTransation()) {
                log.debug("参与tcc事务" + EasyTccContext.getGroupId());
            } else {
                log.debug("开始事务");
            }
            super.doBegin(transaction, definition);
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
            if (EasyTccContext.isTransation()) {
                log.debug("提交sirius事务" + EasyTccContext.getGroupId());
            } else {
                log.debug("提交事务");
            }
            super.doCommit(status);
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
            if (EasyTccContext.isTransation()) {
                log.info("回滚tcc事务" + EasyTccContext.getGroupId());
            } else {
                log.info("回滚事务");
            }
            super.doRollback(status);
        }
    }

}
