package com.huoli.hlai.generate.article.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 文章生成模块的 OpenAPI 配置类。
 * 用于配置 Swagger/OpenAPI 文档设置，包括 API 分组和自定义 API 信息。
 */
@Configuration
public class OpenApiConfig {

    /**
     * 配置自定义的 OpenAPI 实例，设置 API 元数据。
     * 包括 API 标题、版本和描述信息。
     *
     * @return 包含自定义信息的 OpenAPI 实例
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Generate Article API")
                        .version("1.0.0")
                        .description("generate-article module api docs"));
    }

    /**
     * 配置 OpenAPICustomizer，用于自定义 OpenAPI 文档
     * @return OpenAPICustomizer 实例
     */
    @Bean
    public OpenApiCustomizer operationOrderCustomizer() {
        return openAPI -> {
            Paths paths = openAPI.getPaths();
            if (paths == null) {
                return;
            }

            // 获取所有路径条目
            List<Map.Entry<String, PathItem>> pathEntries = new ArrayList<>(paths.entrySet());

            // 按照 operationId 中的数字前缀排序
            pathEntries.sort(Comparator.comparing(entry -> {
                PathItem pathItem = entry.getValue();
                Operation operation = pathItem.getPost();
                if (null == operation)
                {
                    operation = pathItem.getGet();
                }

                if (operation != null && operation.getOperationId() != null) {
                    String operationId = operation.getOperationId();
                    // 提取 operationId 中的数字前缀作为排序值
                    try {
                        // 查找第一个非数字字符的位置
                        int index = 0;
                        while (index < operationId.length() && Character.isDigit(operationId.charAt(index))) {
                            index++;
                        }
                        if (index > 0) {
                            return Integer.parseInt(operationId.substring(0, index));
                        }
                    } catch (NumberFormatException e) {
                        // 解析失败，返回最大值，放在最后
                        return Integer.MAX_VALUE;
                    }
                }

                // 没有 operationId 或解析失败，返回最大值，放在最后
                return Integer.MAX_VALUE;
            }));

            // 创建新的 Paths 对象
            Paths sortedPaths = new Paths();

            // 按照排序后的顺序添加路径
            for (Map.Entry<String, PathItem> entry : pathEntries) {
                sortedPaths.addPathItem(entry.getKey(), entry.getValue());
            }

            // 替换原有的 paths
            openAPI.setPaths(sortedPaths);
        };
    }
}