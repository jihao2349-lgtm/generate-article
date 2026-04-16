package com.huoli.hlai.generate.article.model.vo.hotspot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热点来源类型 VO。
 * 用于返回热点来源类型的枚举列表。
 *
 * @author jihao
 * @date 2026/03/23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotspotSourceTypeVO {

    /**
     * 来源编码
     */
    private String code;

    /**
     * 来源名称
     */
    private String name;
}
