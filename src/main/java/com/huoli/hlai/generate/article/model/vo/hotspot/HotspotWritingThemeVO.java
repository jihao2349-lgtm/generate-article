package com.huoli.hlai.generate.article.model.vo.hotspot;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热点写作主题 VO。
 * 用于封装写作主题的详细信息，包括序号、标题、内容、核心论点和示例列表。
 *
 * @author jihao
 * @date 2026/03/12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotspotWritingThemeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主题序号
     */
    private Integer index;

    /**
     * 主题标题
     */
    private String title;

    /**
     * 主题内容描述
     */
    private String content;

    /**
     * 核心论点
     */
    private String corePoint;

    /**
     * 写作素材示例列表
     */
    private List<HotspotWritingExampleVO> exampleList;
}
