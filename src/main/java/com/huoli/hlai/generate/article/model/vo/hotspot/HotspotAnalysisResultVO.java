package com.huoli.hlai.generate.article.model.vo.hotspot;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热点分析结果 VO。
 * 用于封装热点分析的完整结果，包括热点列表、核心观点、争议性视角和写作指导。
 *
 * @author jihao
 * @date 2026/03/12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotspotAnalysisResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 热点信息列表
     */
    private List<HotspotInfoVO> hotList;

    /**
     * 核心观点列表
     */
    private List<HotspotInsightVO> coreOpinionList;

    /**
     * 争议性视角列表
     */
    private List<HotspotInsightVO> controversialPerspectiveList;

    /**
     * 写作指导建议
     */
    private HotspotWritingGuidanceVO writingGuidanceList;

    /**
     * 创建空的热点分析结果对象。
     *
     * @return 空的 HotspotAnalysisResultVO 实例
     */
    public static HotspotAnalysisResultVO empty() {
        return HotspotAnalysisResultVO.builder()
                .hotList(List.of())
                .coreOpinionList(List.of())
                .controversialPerspectiveList(List.of())
                .writingGuidanceList(HotspotWritingGuidanceVO.empty())
                .build();
    }
}
