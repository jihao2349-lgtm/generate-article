package com.huoli.hlai.generate.article.model.dto.article.template;

import com.huoli.hlai.generate.article.model.dto.UserInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 文章模板ID参数DTO
 */
@Schema(name = "ArticleTemplateIdDTO", description = "文章模板ID参数")
public class ArticleTemplateIdDTO extends UserInfoDTO {
    
    /**
     * 文章模板ID
     */
    @Schema(description = "文章模板ID", example = "template_123456")
    @NotBlank(message = "文章模板ID不能为空")
    private String articleTemplateId;
    
    public String getArticleTemplateId() {
        return articleTemplateId;
    }
    
    public void setArticleTemplateId(String articleTemplateId) {
        this.articleTemplateId = articleTemplateId;
    }
}