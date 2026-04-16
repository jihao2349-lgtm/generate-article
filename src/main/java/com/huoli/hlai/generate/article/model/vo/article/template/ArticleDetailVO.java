package com.huoli.hlai.generate.article.model.vo.article.template;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 文章详情VO
 * 用于返回文章详情信息，包括文章模板来源、链接
 */
@Schema(name = "ArticleDetailVO", description = "文章详情信息")
public class ArticleDetailVO {
    
    /**
     * 文章模板来源（url或file）
     */
    @Schema(description = "文章模板来源（url或file）")
    private String articleTemplateSource;
    
    /**
     * 文章模板链接（URL或文件下载链接）
     */
    @Schema(description = "文章模板链接（URL或文件下载链接）")
    private String articleTemplateUrl;
    

    public String getArticleTemplateSource() {
        return articleTemplateSource;
    }
    
    public void setArticleTemplateSource(String articleTemplateSource) {
        this.articleTemplateSource = articleTemplateSource;
    }
    
    public String getArticleTemplateUrl() {
        return articleTemplateUrl;
    }
    
    public void setArticleTemplateUrl(String articleTemplateUrl) {
        this.articleTemplateUrl = articleTemplateUrl;
    }
}