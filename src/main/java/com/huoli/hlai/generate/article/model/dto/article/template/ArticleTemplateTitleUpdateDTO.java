package com.huoli.hlai.generate.article.model.dto.article.template;

import com.huoli.hlai.generate.article.model.dto.UserInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 文章模板标题更新DTO
 * 用于文章模板标题编辑接口
 */
@Schema(name = "ArticleTemplateTitleUpdateDTO", description = "文章模板标题更新参数")
public class ArticleTemplateTitleUpdateDTO extends UserInfoDTO
{
    /**
     * 文章模板ID
     */
    @Schema(description = "文章模板ID", example = "template_123456")
    @NotBlank(message = "文章模板ID不能为空")
    private String articleTemplateId;

    /**
     * 模板标题
     */
    @Schema(description = "模板标题", example = "人工智能发展趋势分析")
    @NotBlank(message = "模板标题不能为空")
    @Size(min = 1, max = 250, message = "模板标题长度必须在 1 到 250 个字符之间")
    private String templateTitle;

    public String getArticleTemplateId() {
        return articleTemplateId;
    }

    public void setArticleTemplateId(String articleTemplateId) {
        this.articleTemplateId = articleTemplateId;
    }

    public String getTemplateTitle() {
        return templateTitle;
    }

    public void setTemplateTitle(String templateTitle) {
        this.templateTitle = templateTitle;
    }
}