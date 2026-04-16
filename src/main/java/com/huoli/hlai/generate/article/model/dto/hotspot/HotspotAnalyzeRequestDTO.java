package com.huoli.hlai.generate.article.model.dto.hotspot;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import com.huoli.hlai.generate.article.model.dto.UserInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

/**
 * 热点分析请求数据传输对象。
 * 用于接收热点分析接口的请求参数，包含待分析的热点来源列表。
 *
 * @author jihao
 * @date 2026/03/12
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "HotspotAnalyzeRequestDTO", description = "热点分析请求数据传输对象")
public class HotspotAnalyzeRequestDTO extends UserInfoDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 热点来源列表（待分析的热点条目集合）
     */
    @Valid
    @NotEmpty(message = "热点信息不能为空")
    private List<HotspotSourceDTO> hotList;

    /**
     * AI 模型类型（可选，默认 DEEPSEEK）
     * 支持：DEEPSEEK, KIMI, DOUBAO
     */
    private String modelType;
}
