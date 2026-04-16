package com.huoli.hlai.generate.article.model.dto.hotspot;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 原始热点数据传输对象。
 * 用于封装从各个平台抓取的原始热点数据。
 *
 * @author jihao
 * @date 2026/03/12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawHotspotItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 热点标题
     */
    private String title;

    /**
     * 热点内容描述
     */
    private String content;

    /**
     * 来源平台（微博/知乎/抖音等）
     */
    private String source;

    /**
     * 原文链接
     */
    private String url;

    /**
     * 发布时间
     */
    private Date publishTime;

    /**
     * 点赞数
     */
    private Long likeCount;

    /**
     * 评论数
     */
    private Long commentCount;

    /**
     * 转发/分享数
     */
    private Long shareCount;

    /**
     * 搜索指数（可选）
     */
    private Integer searchIndex;

    /**
     * 平台类型标识
     */
    private String platformType;
}
