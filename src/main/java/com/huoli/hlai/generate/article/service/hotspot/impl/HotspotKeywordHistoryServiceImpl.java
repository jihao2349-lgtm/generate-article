package com.huoli.hlai.generate.article.service.hotspot.impl;

import java.time.LocalDateTime;
import java.util.List;

import com.huoli.hlai.generate.article.model.entity.HotspotKeywordHistoryEntity;
import com.huoli.hlai.generate.article.mapper.HotspotKeywordHistoryMapper;
import com.huoli.hlai.generate.article.service.hotspot.HotspotKeywordHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 热点关键词历史服务实现类。
 * 提供热点关键词历史的创建、查询、更新及次数累加等业务操作的具体实现。
 *
 * @author jihao
 * @date 2026/03/12
 */
@Service
@RequiredArgsConstructor
public class HotspotKeywordHistoryServiceImpl implements HotspotKeywordHistoryService {

    private static final int DEFAULT_LIMIT = 10;

    private final HotspotKeywordHistoryMapper hotspotKeywordHistoryMapper;

    /**
     * 创建热点关键词历史记录。
     * 校验必填字段后插入数据库，返回生成的主键 ID。
     *
     * @param entity 热点关键词历史实体
     * @return 创建后的记录 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createHistory(HotspotKeywordHistoryEntity entity) {
        Assert.notNull(entity, "hotspotKeywordHistoryEntity must not be null");
        Assert.notNull(entity.getUserId(), "userId must not be null");
        Assert.hasText(entity.getKeyword(), "keyword must not be blank");

        hotspotKeywordHistoryMapper.insert(entity);
        return entity.getId();
    }

    /**
     * 根据主键 ID 查询热点关键词历史记录。
     *
     * @param id 主键 ID
     * @return 热点关键词历史实体
     */
    @Override
    @Transactional(readOnly = true)
    public HotspotKeywordHistoryEntity getById(Long id) {
        Assert.notNull(id, "id must not be null");
        return hotspotKeywordHistoryMapper.selectById(id);
    }

    /**
     * 根据用户 ID 和关键词查询热点关键词历史记录。
     *
     * @param userId  用户 ID
     * @param keyword 关键词
     * @return 热点关键词历史实体
     */
    @Override
    @Transactional(readOnly = true)
    public HotspotKeywordHistoryEntity getByUserIdAndKeyword(Long userId, String keyword) {
        Assert.notNull(userId, "userId must not be null");
        Assert.hasText(keyword, "keyword must not be blank");
        return hotspotKeywordHistoryMapper.selectByUserIdAndKeyword(userId, keyword);
    }

    /**
     * 根据用户 ID 查询最近使用的关键词列表（按使用时间倒序）。
     * 如果未指定 limit，默认返回 10 条记录。
     *
     * @param userId 用户 ID
     * @param limit  返回数量限制
     * @return 热点关键词历史实体列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<HotspotKeywordHistoryEntity> listLatestByUserId(Long userId, Integer limit) {
        Assert.notNull(userId, "userId must not be null");
        int queryLimit = limit == null || limit <= 0 ? DEFAULT_LIMIT : limit;
        return hotspotKeywordHistoryMapper.selectLatestByUserId(userId, queryLimit);
    }

    /**
     * 根据主键 ID 选择性更新热点关键词历史记录（只更新非 null 字段）。
     *
     * @param entity 热点关键词历史实体
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateByIdSelective(HotspotKeywordHistoryEntity entity) {
        Assert.notNull(entity, "hotspotKeywordHistoryEntity must not be null");
        Assert.notNull(entity.getId(), "id must not be null");
        return hotspotKeywordHistoryMapper.updateByIdSelective(entity) > 0;
    }

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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrIncrement(Long userId, String keyword, LocalDateTime lastUsedTime) {
        Assert.notNull(userId, "userId must not be null");
        Assert.hasText(keyword, "keyword must not be blank");
        LocalDateTime finalLastUsedTime = lastUsedTime == null ? LocalDateTime.now() : lastUsedTime;
        return hotspotKeywordHistoryMapper.insertOrIncrement(userId, keyword, finalLastUsedTime) > 0;
    }
}
