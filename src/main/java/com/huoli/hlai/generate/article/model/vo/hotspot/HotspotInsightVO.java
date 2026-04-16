package com.huoli.hlai.generate.article.model.vo.hotspot;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热点洞察 VO。
 * 用于封装核心观点或争议性视角的标题和内容。
 *
 * @author jihao
 * @date 2026/03/12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotspotInsightVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 洞察序号
     */
    private Integer index;

    /**
     * 洞察标题
     */
    private String title;

    /**
     * 洞察内容详情
     */
    private String content;
}
