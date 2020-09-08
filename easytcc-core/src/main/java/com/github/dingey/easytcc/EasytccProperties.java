package com.github.dingey.easytcc;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.concurrent.TimeUnit;

@Getter
@Setter
@RefreshScope
@ConfigurationProperties(prefix = "easy.tcc")
public class EasytccProperties {
    /**
     * enable for easy tcc auto configuration
     */
    private boolean enable = true;

    private Client client = new Client();

    private Server server = new Server();

    @Getter
    @Setter
    public static class Client {
        /**
         * set Transaction Manager id | 事务服务器服务ID
         */
        private String serverId = "easytcc-server";

        /**
         * Asynchronous notification compensation | 异步通知服务端
         */
        private boolean async = true;
        /**
         * set thread pool config | 设置线程池参数
         */
        private ThreadPool threadPool = new ThreadPool();
    }

    @Getter
    @Setter
    public static class Server {

        /**
         * Asynchronous notification compensation | 异步通知补偿操作
         */
        private boolean async = true;
        /**
         * set thread pool config | 设置线程池参数
         */
        private ThreadPool threadPool = new ThreadPool();
        /**
         * set Persistence type | 设置持久化
         */
        private Store store = Store.NONE;
    }

    @Getter
    @Setter
    public static class ThreadPool {
        /**
         * the number of threads to keep in the pool
         */
        private int corePoolSize = 2;
        /**
         * the maximum number of threads to allow in the  pool
         */
        private int maximumPoolSize = 5;
        /**
         * when the number of threads is greater than
         * the core, this is the maximum time that excess idle threads
         * will wait for new tasks before terminating.
         */
        private long keepAliveTime = 30;
        /**
         * the time unit for the {@code keepAliveTime} argument
         */
        private TimeUnit unit = TimeUnit.SECONDS;
        /**
         * set the queue size to use for holding tasks before they are executed.
         */
        private int capacity = 20;
    }

    @SuppressWarnings("unused")
    enum Store {
        /**
         * no store | 不持久化
         */
        NONE,
        /**
         * redis缓存
         */
        REDIS,
        /**
         * database 数据库
         */
        DB
    }
}
