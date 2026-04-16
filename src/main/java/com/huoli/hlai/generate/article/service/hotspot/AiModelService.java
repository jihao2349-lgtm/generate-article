package com.huoli.hlai.generate.article.service.hotspot;

import com.huoli.hlai.generate.article.model.dto.hotspot.RawHotspotItem;

import java.util.List;

/**
 * AI 模型服务接口。
 * 提供多种 AI 模型的统一调用规范，支持 DeepSeek、Kimi、Doubao 等模型。
 *
 * @author jihao
 * @date 2026/03/13
 */
public interface AiModelService {

    /**
     * 判断当前服务是否支持该 AI 模型
     *
     * @param modelType 模型类型标识（如：DEEPSEEK, KIMI, DOUBAO）
     * @return true-支持；false-不支持
     */
    boolean supports(String modelType);

    /**
     * 对热点数据进行分析
     *
     * @param keywords 关键词列表
     * @param hotspots 原始热点数据列表
     * @return AI 分析结果
     */
    String analyze(List<String> keywords, List<RawHotspotItem> hotspots);

    /**
     * 获取模型类型标识
     *
     * @return 模型名称（如：DEEPSEEK, KIMI, DOUBAO）
     */
    String getModelType();
}
