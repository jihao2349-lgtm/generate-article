package com.huoli.hlai.generate.article.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务线程池配置类
 *
 * @date 2026/03/19
 */
@Configuration
public class ThreadPoolConfig {

	/**
	 * 文章解析专用线程池
	 * 根据阿里巴巴规范，必须显式配置线程池参数
	 */
	@Bean("articleParseExecutor")
	public Executor articleParseExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(10);
		executor.setQueueCapacity(50);
		executor.setKeepAliveSeconds(60);
		executor.setThreadNamePrefix("article-parse-");
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(120);
		executor.initialize();

		return executor;
	}
}