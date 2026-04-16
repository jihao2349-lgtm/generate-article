package com.huoli.hlai.generate.article.model.vo.hotspot;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热点信息 VO。
 * 用于封装单个热点条目的基本信息，包括序号、标题、内容、来源和热度值。
 *
 * @author jihao
 * @date 2026/03/12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotspotInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 热点序号（在热点排行榜中的位置）
     */
    private Integer index;

    /**
     * 热点标题
     */
    private String title;

    /**
     * 热点内容描述
     */
    private String content;

    /**
     * 热点来源平台
     */
    private String source;

    /**
     * 热度值（数值越大表示越热门）
     */
    private Integer heat;
}
