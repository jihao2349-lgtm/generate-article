package com.huoli.hlai.generate.article.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器。
 * 提供系统健康状态检查接口，用于监控服务运行状态。
 *
 * @author jihao
 * @date 2026/03/23
 */
@Slf4j
@RestController
@RequestMapping
@Tag(name = "健康检查")
public class HealthCheckController {

    /**
     * 时间格式化器
     */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 健康检查接口。
     * 返回服务的健康状态信息。
     *
     * @return 包含状态、服务名和时间戳的 Map
     */
    @Operation(summary = "健康检查")
    @GetMapping("/healthcheck")
    public Map<String, String> healthCheck() {
        log.info("[健康检查] 收到健康检查请求");
        
        Map<String, String> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "generate-article");
        result.put("timestamp", LocalDateTime.now().format(FORMATTER));
        
        log.info("[健康检查] 健康检查完成，状态：UP");
        return result;
    }
}
