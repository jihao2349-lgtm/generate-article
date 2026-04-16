package com.huoli.hlai.generate.article.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huoli.hlai.generate.article.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户拦截器
 * 从请求体中获取用户ID并存储到UserContext中
 */
@Slf4j
public class UserInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        String userId = UserContext.getUserId();
        if (StringUtils.isBlank(userId)) {
            userId = request.getParameter("userId");
            if (userId != null && !userId.isBlank()) {
                UserContext.setUserId(userId);
                log.info("Interceptor 获取用户 userId: {}", userId);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, Exception ex) throws Exception {
        UserContext.clear();
        log.info("用户请求信息清除");
    }

    /**
     * 判断是否为JSON请求
     */
    private boolean isJsonRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.contains("application/json");
    }

}