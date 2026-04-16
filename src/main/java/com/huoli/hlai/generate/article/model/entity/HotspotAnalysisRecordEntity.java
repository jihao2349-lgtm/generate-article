package com.huoli.hlai.generate.article.model.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热点分析记录实体类。
 * 对应数据库表 hotspot_analysis_record，用于存储用户热点分析请求的完整流程和结果。
 *
 * @author jihao
 * @date 2026/03/12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotspotAnalysisRecordEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 请求流水号（唯一标识一次请求）
     */
    private String requestNo;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 请求类型：KEYWORD（关键词分析）、RAW_LIST（原始列表分析）
     */
    private String requestType;

    /**
     * 热点关键词，多个关键词使用英文逗号分隔
     */
    private String keyword;

    /**
     * 请求摘要哈希（SHA-256），用于缓存命中和幂等判断
     */
    private String requestHash;

    /**
     * 抓取平台列表 JSON 数组
     */
    private String sourcePlatforms;

    /**
     * 原始请求报文 JSON
     */
    private String inputPayload;

    /**
     * 处理状态：INIT（初始）、SUCCESS（成功）、FAILED（失败）
     */
    private String status;

    /**
     * 热点分析结果 JSON
     */
    private String resultJson;

    /**
     * 失败原因描述
     */
    private String errorMessage;

    /**
     * 开始处理时间
     */
    private LocalDateTime startedTime;

    /**
     * 处理完成时间
     */
    private LocalDateTime finishedTime;

    /**
     * 创建时间（数据库自动生成）
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间（数据库自动更新）
     */
    private LocalDateTime updatedTime;
}
