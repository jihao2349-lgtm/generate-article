package com.huoli.hlai.generate.article.service.hotspot;

import com.huoli.hlai.generate.article.model.dto.hotspot.RawHotspotItem;
import com.huoli.hlai.generate.article.exception.AiAnalysisException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI 模型服务协调器。
 * 负责根据模型类型选择合适的 AI 服务进行分析。
 *
 * @author jihao
 * @date 2026/03/13
 */
@Slf4j
@Component
public class AiModelCoordinator {

    @Autowired
    List<AiModelService> aiServices;

    /**
     * 根据模型类型选择合适的 AI 服务进行分析
     *
     * @param modelType 模型类型（DEEPSEEK, KIMI, DOUBAO）
     * @param keywords  关键词列表
     * @param hotspots  原始热点数据列表
     * @return AI 分析结果
     * @throws AiAnalysisException 当 AI 分析失败时抛出异常
     */
    public String analyze(String modelType, List<String> keywords, List<RawHotspotItem> hotspots) {
        log.info("[AI 协调器] 开始分析，模型类型：{}, 热点数量：{}", modelType, hotspots.size());

        // 查找支持的 AI 服务
        AiModelService selectedService = aiServices.stream()
                .filter(service -> service.supports(modelType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("不支持的 AI 模型类型：" + modelType));

        log.info("[AI 协调器] 使用 AI 服务：{}", selectedService.getClass().getSimpleName());

        // 调用 AI 服务进行分析（异常会直接传播）
        return selectedService.analyze(keywords, hotspots);
    }

    /**
     * 获取所有可用的 AI 模型类型
     *
     * @return AI 模型类型列表
     */
    public List<String> getAvailableModels() {
        return aiServices.stream()
                .map(AiModelService::getModelType)
                .toList();
    }
}
