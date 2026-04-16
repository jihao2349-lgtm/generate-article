package com.huoli.hlai.generate.article.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置类。
 * 配置热点分析任务的线程池。
 *
 * @author jihao
 * @date 2026/03/23
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 热点任务执行器。
     * 用于处理热点抓取和 AI 分析的异步任务。
     *
     * @return 线程池执行器
     */
    @Bean(name = "hotspotTaskExecutor")
    public Executor hotspotTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数
        executor.setCorePoolSize(5);
        
        // 最大线程数
        executor.setMaxPoolSize(10);
        
        // 队列容量
        executor.setQueueCapacity(50);
        
        // 线程名前缀
        executor.setThreadNamePrefix("hotspot-task-");
        
        // 拒绝策略：CallerRunsPolicy - 由调用线程处理
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 空闲线程存活时间（秒）
        executor.setKeepAliveSeconds(60);
        
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间（秒）
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        
        log.info("[异步配置] 热点任务线程池初始化完成，核心线程数：5, 最大线程数：10, 队列容量：50");
        
        return executor;
    }
}
