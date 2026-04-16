package com.huoli.hlai.generate.article.model.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热点关键词历史实体类。
 * 对应数据库表 hotspot_keyword_history，用于记录用户使用过的热点关键词及使用频次。
 *
 * @author jihao
 * @date 2026/03/12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotspotKeywordHistoryEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 关键词
     */
    private String keyword;

    /**
     * 使用次数（每次使用累加）
     */
    private Integer usedCount;

    /**
     * 最近使用时间
     */
    private LocalDateTime lastUsedTime;

    /**
     * 创建时间（数据库自动生成）
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间（数据库自动更新）
     */
    private LocalDateTime updatedTime;
}
