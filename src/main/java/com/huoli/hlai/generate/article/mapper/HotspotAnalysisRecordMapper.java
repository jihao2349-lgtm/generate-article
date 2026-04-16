package com.huoli.hlai.generate.article.mapper;

import java.time.LocalDateTime;

import com.huoli.hlai.generate.article.model.entity.HotspotAnalysisRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 热点分析记录 Mapper 接口。
 * 用于操作 hotspot_analysis_record 表的数据访问层。
 *
 * @author jihao
 * @date 2026/03/12
 */
@Mapper
public interface HotspotAnalysisRecordMapper {

    /**
     * 插入热点分析记录。
     *
     * @param entity 热点分析记录实体
     * @return 影响的行数
     */
    int insert(HotspotAnalysisRecordEntity entity);

    /**
     * 根据主键 ID 查询热点分析记录。
     *
     * @param id 主键 ID
     * @return 热点分析记录实体
     */
    HotspotAnalysisRecordEntity selectById(@Param("id") Long id);

    /**
     * 根据请求流水号查询热点分析记录。
     *
     * @param requestNo 请求流水号
     * @return 热点分析记录实体
     */
    HotspotAnalysisRecordEntity selectByRequestNo(@Param("requestNo") String requestNo);

    /**
     * 根据请求哈希值查询最近一次成功的热点分析记录（用于缓存命中）。
     *
     * @param requestHash 请求摘要哈希
     * @return 热点分析记录实体
     */
    HotspotAnalysisRecordEntity selectLatestSuccessByRequestHash(@Param("requestHash") String requestHash);

    /**
     * 根据主键 ID 选择性更新热点分析记录（只更新非 null 字段）。
     *
     * @param entity 热点分析记录实体
     * @return 影响的行数
     */
    int updateByIdSelective(HotspotAnalysisRecordEntity entity);

    /**
     * 根据主键 ID 更新处理状态和结果。
     *
     * @param id 主键 ID
     * @param status 处理状态
     * @param resultJson 热点分析结果 JSON
     * @param errorMessage 失败原因
     * @param finishedTime 处理完成时间
     * @return 影响的行数
     */
    int updateStatusById(@Param("id") Long id,
                         @Param("status") String status,
                         @Param("resultJson") String resultJson,
                         @Param("errorMessage") String errorMessage,
                         @Param("finishedTime") LocalDateTime finishedTime);
}
