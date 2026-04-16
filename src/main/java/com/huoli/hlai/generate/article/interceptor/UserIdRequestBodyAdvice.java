package com.huoli.hlai.generate.article.interceptor;

import com.huoli.hlai.generate.article.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * @author jihao
 * @since 2026/3/24 11:29
 */
@Slf4j
@ControllerAdvice
public class UserIdRequestBodyAdvice extends RequestBodyAdviceAdapter {

    @Autowired
    private HttpServletRequest request;

    @Override
    public boolean supports(@NotNull MethodParameter methodParameter, @NotNull Type targetType,
                            @NotNull Class<? extends HttpMessageConverter<?>> converterType) {
        return true; // 拦截所有请求体
    }

    @NotNull
    @Override
    public Object afterBodyRead(@NotNull Object body, @NotNull HttpInputMessage inputMessage,
                                @NotNull MethodParameter parameter, @NotNull Type targetType,
                                @NotNull Class<? extends HttpMessageConverter<?>> converterType) {
        try {
            String userId = getFieldValue(body);
            if (userId == null || userId.isEmpty()) {
                userId = request.getParameter("userId");
            }
            if (userId != null && !userId.isEmpty()) {
                UserContext.setUserId(userId);
                log.info("UserIdRequestBodyAdvice 获取用户 userId: {}", userId);
            }
        } catch (Exception e) {
            log.warn("获取userId失败: {}", e.getMessage());
        }
        return body;
    }

    /**
     * 向上遍历父类查找字段
     */
    private String getFieldValue(Object obj) {
        Class<?> clazz = obj.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField("userId");
                field.setAccessible(true);
                Object value = field.get(obj);
                return value != null ? value.toString() : null;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass(); // 找不到就去父类找
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
