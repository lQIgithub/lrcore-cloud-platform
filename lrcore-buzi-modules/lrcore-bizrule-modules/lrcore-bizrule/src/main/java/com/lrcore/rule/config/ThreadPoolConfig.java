package com.lrcore.rule.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.*;

/**
 * <p>类模块说明</p>

 * @Describe: 线程池配置
 * @ClassName: ThreadPoolConfig
 * @Author: Qi Liu
 * @Date: 2026/8/5 10:09
 * @Version: 1.0
 */
@Configuration
public class ThreadPoolConfig {

    /**
     * 【管理线程池】专门执行Jar扫描、热更新reload，和Tomcat完全隔离
     */
    @Bean("ruleManageExecutor")
    public ExecutorService ruleManageExecutor() {
        return new ThreadPoolExecutor(
                2,
                4,
                30,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * 【规则执行隔离线程池】执行 rule.validate()，防止用户规则死循环占满Tomcat
     */
    @Bean("ruleRunExecutor")
    public ExecutorService ruleRunExecutor() {
        return new ThreadPoolExecutor(
                8,
                16,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}