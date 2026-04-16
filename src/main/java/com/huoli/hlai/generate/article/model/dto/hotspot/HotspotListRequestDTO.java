package com.huoli.hlai.generate.article.model.dto.hotspot;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import com.huoli.hlai.generate.article.model.dto.UserInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 热点列表请求数据传输对象。
 * 用于接收热点抓取接口的请求参数，包含要抓取的热点关键词。
 *
 * @author jihao
 * @date 2026/03/12
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "HotspotListRequestDTO", description = "热点列表请求数据传输对象")
public class HotspotListRequestDTO extends UserInfoDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 热点关键词（用于筛选和抓取相关热点内容）
     */
    @NotBlank(message = "热点关键词不能为空")
    private String keyword;
    
    /**
     * AI 模型类型（可选，默认 DEEPSEEK）
     * 支持：DEEPSEEK, KIMI, DOUBAO
     */
    private String modelType;

    /**
     * AI 抓取来源类型列表（可选，支持多选），对应 HotSpotSourceTypes.code
     * 支持：xiaohonshu、weibo、jinritoutiao、baijiahao、zhihu
     * 不传或为空则默认全网抓取
     */
    @Schema(description = "AI 抓取来源类型列表，支持多选，对应平台 code（xiaohonshu/weibo/jinritoutiao/baijiahao/zhihu），不传或为空则全网抓取")
    private List<String> sources;
}
