package com.huoli.hlai.generate.article.mapper;

import java.time.LocalDateTime;
import java.util.List;

import com.huoli.hlai.generate.article.model.entity.HotspotKeywordHistoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 热点关键词历史 Mapper 接口。
 * 用于操作 hotspot_keyword_history 表的数据访问层。
 *
 * @author jihao
 * @date 2026/03/12
 */
@Mapper
public interface HotspotKeywordHistoryMapper {

    /**
     * 插入热点关键词历史记录。
     *
     * @param entity 热点关键词历史实体
     * @return 影响的行数
     */
    int insert(HotspotKeywordHistoryEntity entity);

    /**
     * 根据主键 ID 查询热点关键词历史记录。
     *
     * @param id 主键 ID
     * @return 热点关键词历史实体
     */
    HotspotKeywordHistoryEntity selectById(@Param("id") Long id);

    /**
     * 根据用户 ID 和关键词查询热点关键词历史记录。
     *
     * @param userId 用户 ID
     * @param keyword 关键词
     * @return 热点关键词历史实体
     */
    HotspotKeywordHistoryEntity selectByUserIdAndKeyword(@Param("userId") Long userId,
                                                         @Param("keyword") String keyword);

    /**
     * 根据用户 ID 查询最近使用的关键词列表（按使用时间倒序）。
     *
     * @param userId 用户 ID
     * @param limit 返回数量限制
     * @return 热点关键词历史实体列表
     */
    List<HotspotKeywordHistoryEntity> selectLatestByUserId(@Param("userId") Long userId,
                                                           @Param("limit") Integer limit);

    /**
     * 根据主键 ID 选择性更新热点关键词历史记录（只更新非 null 字段）。
     *
     * @param entity 热点关键词历史实体
     * @return 影响的行数
     */
    int updateByIdSelective(HotspotKeywordHistoryEntity entity);

    /**
     * 插入或累加关键词使用次数（幂等操作）。
     * 如果关键词已存在，则累加使用次数并更新最近使用时间；
     * 如果关键词不存在，则插入新记录。
     *
     * @param userId 用户 ID
     * @param keyword 关键词
     * @param lastUsedTime 最近使用时间
     * @return 影响的行数
     */
    int insertOrIncrement(@Param("userId") Long userId,
                          @Param("keyword") String keyword,
                          @Param("lastUsedTime") LocalDateTime lastUsedTime);
}
