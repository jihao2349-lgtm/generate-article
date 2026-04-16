package com.huoli.hlai.generate.article.model.dto.hotspot;

import java.io.Serial;
import java.io.Serializable;

import com.huoli.hlai.generate.article.model.dto.UserInfoDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * 热点来源数据传输对象。
 * 用于封装单个热点条目的详细信息，包括序号、标题、内容和来源平台。
 *
 * @author jihao
 * @date 2026/03/12
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotspotSourceDTO extends UserInfoDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 热点序号（在热点列表中的排序位置）
     */
    @NotNull(message = "热点序号不能为空")
    private Integer index;

    /**
     * 热点标题
     */
    @NotBlank(message = "热点标题不能为空")
    private String title;

    /**
     * 热点内容描述
     */
    @NotBlank(message = "热点内容不能为空")
    private String content;

    /**
     * 热点来源平台（如微博、知乎、抖音等）
     */
    @NotBlank(message = "热点来源平台不能为空")
    private String source;
    
    /**
     * 发布时间（可选，格式：yyyy-MM-dd HH:mm:ss）
     */
    private String publishTime;
    
    /**
     * 点赞数（可选，用于计算热度）
     */
    private Long likeCount;
    
    /**
     * 评论数（可选，用于计算热度）
     */
    private Long commentCount;
    
    /**
     * 转发数（可选，用于计算热度）
     */
    private Long shareCount;
}
