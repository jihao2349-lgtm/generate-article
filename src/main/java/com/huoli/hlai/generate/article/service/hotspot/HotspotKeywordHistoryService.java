package com.huoli.hlai.generate.article.service.hotspot;

import java.time.LocalDateTime;
import java.util.List;

import com.huoli.hlai.generate.article.model.entity.HotspotKeywordHistoryEntity;

/**
 * 热点关键词历史服务接口。
 * 提供热点关键词历史的创建、查询、更新及次数累加等业务操作。
 *
 * @author jihao
 * @date 2026/03/12
 */
public interface HotspotKeywordHistoryService {

    /**
     * 创建热点关键词历史记录。
     *
     * @param entity 热点关键词历史实体
     * @return 创建后的记录 ID
     */
    Long createHistory(HotspotKeywordHistoryEntity entity);

    /**
     * 根据主键 ID 查询热点关键词历史记录。
     *
     * @param id 主键 ID
     * @return 热点关键词历史实体
     */
    HotspotKeywordHistoryEntity getById(Long id);

    /**
     * 根据用户 ID 和关键词查询热点关键词历史记录。
     *
     * @param userId  用户 ID
     * @param keyword 关键词
     * @return 热点关键词历史实体
     */
    HotspotKeywordHistoryEntity getByUserIdAndKeyword(Long userId, String keyword);

    /**
     * 根据用户 ID 查询最近使用的关键词列表（按使用时间倒序）。
     *
     * @param userId 用户 ID
     * @param limit  返回数量限制
     * @return 热点关键词历史实体列表
     */
    List<HotspotKeywordHistoryEntity> listLatestByUserId(Long userId, Integer limit);

    /**
     * 根据主键 ID 选择性更新热点关键词历史记录（只更新非 null 字段）。
     *
     * @param entity 热点关键词历史实体
     * @return 是否更新成功
     */
    boolean updateByIdSelective(HotspotKeywordHistoryEntity entity);

    /**
     * 保存或累加关键词使用次数（幂等操作）。
     * 如果关键词已存在，则累加使用次数并更新最近使用时间；
     * 如果关键词不存在，则插入新记录。
     *
     * @param userId       用户 ID
     * @param keyword      关键词
     * @param lastUsedTime 最近使用时间
     * @return 是否操作成功
     */
    boolean saveOrIncrement(Long userId, String keyword, LocalDateTime lastUsedTime);
}
