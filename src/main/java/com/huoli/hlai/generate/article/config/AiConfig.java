package com.huoli.hlai.generate.article.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI Alibaba 配置类。
 * 配置 DeepSeek 模型和百度搜索工具。
 *
 * @author jihao
 * @date 2026/03/12
 */
@Configuration
public class AiConfig {

    @Value("${spring.ai.dashscope.api-key:}")
    private String dashScopeApiKey;

    @Value("${spring.ai.dashscope.base-url:https://api.deepseek.com}")
    private String dashScopeBaseUrl;

    /**
     * 创建 ChatModel Bean（使用 DeepSeek 模型）
     */
    @Bean
    public ChatModel chatModel() {
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(dashScopeApiKey)
                .baseUrl(dashScopeBaseUrl)
                .build();

        return DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .build();
    }

    /**
     * 创建 ChatClient Bean（高级 API）
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
