package com.huoli.hlai.generate.article.config;

import com.huoli.hlai.generate.article.interceptor.UserInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类。
 * 用于配置跨域（CORS）设置，允许指定的源和方法访问文章生成模块的接口。
 *
 * @author jihao
 * @date 2026/03/12
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 允许的源地址模式数组。
     * 支持本地开发环境的 localhost 和 127.0.0.1，端口不限。
     */
    private static final String[] ALLOWED_ORIGIN_PATTERNS = {
            "http://localhost:*",
            "http://127.0.0.1:*"
    };

    /**
     * 允许的 HTTP 方法数组。
     * 包括 GET、POST、PUT、DELETE 和 OPTIONS 方法。
     */
    private static final String[] ALLOWED_METHODS = {
            "GET",
            "POST",
            "PUT",
            "DELETE",
            "OPTIONS"
    };

    /**
     * 配置跨域映射规则。
     * 针对 /api/** 路径下的所有接口设置跨域访问权限。
     *
     * @param registry CorsRegistry 注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(ALLOWED_ORIGIN_PATTERNS)
                .allowedMethods(ALLOWED_METHODS)
                .allowedHeaders("*")
                .exposedHeaders("Content-Disposition")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/healthcheck");
    }
}
