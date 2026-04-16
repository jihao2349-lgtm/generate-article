package com.huoli.hlai.generate.article.service.article.template;

import com.huoli.hlai.generate.article.model.entity.ArticleTemplate;

import java.util.List;

/**
 * 文章解析服务接口
 */
public interface ArticleParserService {

    /**
     * 智能拆解文章URL
     * @param articleTemplates 文章模板列表
     */
    void parseUrl(List<ArticleTemplate> articleTemplates);

    /**
     * 解析文章
     * @param articleTemplateId 文章模板ID
     * @param userId 用户ID
     */
    void parseArticle(String articleTemplateId, String userId);
}