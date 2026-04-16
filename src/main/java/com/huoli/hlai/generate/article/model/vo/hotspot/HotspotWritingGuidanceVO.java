package com.huoli.hlai.generate.article.model.vo.hotspot;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热点写作指导 VO。
 * 用于封装写作指导建议，包含轻度和重度两个层次的写作主题列表。
 *
 * @author jihao
 * @date 2026/03/12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotspotWritingGuidanceVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 轻度写作主题列表（适合快速成文）
     */
    private List<HotspotWritingThemeVO> lightList;

    /**
     * 重度写作主题列表（适合深度分析）
     */
    private List<HotspotWritingThemeVO> heavyList;

    /**
     * 创建空的写作指导对象。
     *
     * @return 空的 HotspotWritingGuidanceVO 实例
     */
    public static HotspotWritingGuidanceVO empty() {
        return HotspotWritingGuidanceVO.builder()
                .lightList(List.of())
                .heavyList(List.of())
                .build();
    }
}
