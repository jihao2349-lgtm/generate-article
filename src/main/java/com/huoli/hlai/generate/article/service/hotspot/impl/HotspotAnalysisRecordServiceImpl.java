package com.huoli.hlai.generate.article.service.hotspot.impl;

import java.time.LocalDateTime;

import com.huoli.hlai.generate.article.model.entity.HotspotAnalysisRecordEntity;
import com.huoli.hlai.generate.article.mapper.HotspotAnalysisRecordMapper;
import com.huoli.hlai.generate.article.service.hotspot.HotspotAnalysisRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 热点分析记录服务实现类。
 * 提供热点分析记录的创建、查询、更新等业务操作的具体实现。
 *
 * @author jihao
 * @date 2026/03/12
 */
@Service
@RequiredArgsConstructor
public class HotspotAnalysisRecordServiceImpl implements HotspotAnalysisRecordService {

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";

    private final HotspotAnalysisRecordMapper hotspotAnalysisRecordMapper;

    /**
     * 创建热点分析记录。
     * 校验必填字段后插入数据库，返回生成的主键 ID。
     *
     * @param entity 热点分析记录实体
     * @return 创建后的记录 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRecord(HotspotAnalysisRecordEntity entity) {
        Assert.notNull(entity, "hotspotAnalysisRecordEntity must not be null");
        Assert.hasText(entity.getRequestNo(), "requestNo must not be blank");
        Assert.hasText(entity.getRequestType(), "requestType must not be blank");
        Assert.hasText(entity.getRequestHash(), "requestHash must not be blank");
        Assert.hasText(entity.getInputPayload(), "inputPayload must not be blank");

        hotspotAnalysisRecordMapper.insert(entity);
        return entity.getId();
    }

    /**
     * 根据主键 ID 查询热点分析记录。
     *
     * @param id 主键 ID
     * @return 热点分析记录实体
     */
    @Override
    @Transactional(readOnly = true)
    public HotspotAnalysisRecordEntity getById(Long id) {
        Assert.notNull(id, "id must not be null");
        return hotspotAnalysisRecordMapper.selectById(id);
    }

    /**
     * 根据请求流水号查询热点分析记录。
     *
     * @param requestNo 请求流水号
     * @return 热点分析记录实体
     */
    @Override
    @Transactional(readOnly = true)
    public HotspotAnalysisRecordEntity getByRequestNo(String requestNo) {
        Assert.hasText(requestNo, "requestNo must not be blank");
        return hotspotAnalysisRecordMapper.selectByRequestNo(requestNo);
    }

    /**
     * 根据请求哈希值查询最近一次成功的热点分析记录（用于缓存命中）。
     *
     * @param requestHash 请求摘要哈希
     * @return 热点分析记录实体
     */
    @Override
    @Transactional(readOnly = true)
    public HotspotAnalysisRecordEntity getLatestSuccessByRequestHash(String requestHash) {
        Assert.hasText(requestHash, "requestHash must not be blank");
        return hotspotAnalysisRecordMapper.selectLatestSuccessByRequestHash(requestHash);
    }

    /**
     * 根据主键 ID 选择性更新热点分析记录（只更新非 null 字段）。
     *
     * @param entity 热点分析记录实体
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateByIdSelective(HotspotAnalysisRecordEntity entity) {
        Assert.notNull(entity, "hotspotAnalysisRecordEntity must not be null");
        Assert.notNull(entity.getId(), "id must not be null");
        return hotspotAnalysisRecordMapper.updateByIdSelective(entity) > 0;
    }

    /**
     * 标记热点分析记录为成功状态。
     * 更新状态为 SUCCESS，并设置结果 JSON 和完成时间。
     *
     * @param id           主键 ID
     * @param resultJson   热点分析结果 JSON
     * @param finishedTime 处理完成时间
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markSuccess(Long id, String resultJson, LocalDateTime finishedTime) {
        Assert.notNull(id, "id must not be null");
        Assert.hasText(resultJson, "resultJson must not be blank");
        LocalDateTime finalFinishedTime = finishedTime == null ? LocalDateTime.now() : finishedTime;
        return hotspotAnalysisRecordMapper.updateStatusById(
                id,
                STATUS_SUCCESS,
                resultJson,
                null,
                finalFinishedTime
        ) > 0;
    }

    /**
     * 标记热点分析记录为失败状态。
     * 更新状态为 FAILED，并设置错误信息和完成时间。
     *
     * @param id           主键 ID
     * @param errorMessage 失败原因
     * @param finishedTime 处理完成时间
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markFailed(Long id, String errorMessage, LocalDateTime finishedTime) {
        Assert.notNull(id, "id must not be null");
        Assert.hasText(errorMessage, "errorMessage must not be blank");
        LocalDateTime finalFinishedTime = finishedTime == null ? LocalDateTime.now() : finishedTime;
        return hotspotAnalysisRecordMapper.updateStatusById(
                id,
                STATUS_FAILED,
                null,
                errorMessage,
                finalFinishedTime
        ) > 0;
    }
}
