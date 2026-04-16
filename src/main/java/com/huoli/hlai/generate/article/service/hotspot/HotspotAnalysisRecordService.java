package com.huoli.hlai.generate.article.service.hotspot;

import java.time.LocalDateTime;

import com.huoli.hlai.generate.article.model.entity.HotspotAnalysisRecordEntity;

/**
 * 热点分析记录服务接口。
 * 提供热点分析记录的创建、查询、更新等业务操作。
 *
 * @author jihao
 * @date 2026/03/12
 */
public interface HotspotAnalysisRecordService {

    /**
     * 创建热点分析记录。
     *
     * @param entity 热点分析记录实体
     * @return 创建后的记录 ID
     */
    Long createRecord(HotspotAnalysisRecordEntity entity);

    /**
     * 根据主键 ID 查询热点分析记录。
     *
     * @param id 主键 ID
     * @return 热点分析记录实体
     */
    HotspotAnalysisRecordEntity getById(Long id);

    /**
     * 根据请求流水号查询热点分析记录。
     *
     * @param requestNo 请求流水号
     * @return 热点分析记录实体
     */
    HotspotAnalysisRecordEntity getByRequestNo(String requestNo);

    /**
     * 根据请求哈希值查询最近一次成功的热点分析记录（用于缓存命中）。
     *
     * @param requestHash 请求摘要哈希
     * @return 热点分析记录实体
     */
    HotspotAnalysisRecordEntity getLatestSuccessByRequestHash(String requestHash);

    /**
     * 根据主键 ID 选择性更新热点分析记录（只更新非 null 字段）。
     *
     * @param entity 热点分析记录实体
     * @return 是否更新成功
     */
    boolean updateByIdSelective(HotspotAnalysisRecordEntity entity);

    /**
     * 标记热点分析记录为成功状态。
     *
     * @param id           主键 ID
     * @param resultJson   热点分析结果 JSON
     * @param finishedTime 处理完成时间
     * @return 是否更新成功
     */
    boolean markSuccess(Long id, String resultJson, LocalDateTime finishedTime);

    /**
     * 标记热点分析记录为失败状态。
     *
     * @param id           主键 ID
     * @param errorMessage 失败原因
     * @param finishedTime 处理完成时间
     * @return 是否更新成功
     */
    boolean markFailed(Long id, String errorMessage, LocalDateTime finishedTime);
}
