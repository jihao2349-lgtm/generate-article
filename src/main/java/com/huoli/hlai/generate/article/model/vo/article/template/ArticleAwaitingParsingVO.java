package com.huoli.hlai.generate.article.model.vo.article.template;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 待拆解文章VO
 * 用于表示待拆解文章的基本信息
 */
@Schema(name = "ArticleAwaitingParsingVO", description = "待拆解文章信息")
public class ArticleAwaitingParsingVO
{
    
    /**
     * 文章模板ID
     */
    @Schema(description = "文章模板ID")
    private String articleTemplateId;
    
    /**
     * 文章标题
     */
    @Schema(description = "文章标题")
    private String articleTitle;
    
    public String getArticleTemplateId() {
        return articleTemplateId;
    }

    public void setArticleTemplateId(String articleTemplateId) {
        this.articleTemplateId = articleTemplateId;
    }
    
    public String getArticleTitle() {
        return articleTitle;
    }

    public void setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
    }
}