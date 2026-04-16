package com.huoli.hlai.generate.article.model.vo.hotspot;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热点写作示例 VO。
 * 用于封装写作素材示例，包括序号、标题、内容和来源。
 *
 * @author jihao
 * @date 2026/03/12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotspotWritingExampleVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 示例序号
     */
    private Integer index;

    /**
     * 示例标题
     */
    private String title;

    /**
     * 示例内容
     */
    private String content;

    /**
     * 示例来源
     */
    private String source;
}
