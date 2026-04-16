package com.huoli.hlai.generate.article.model.dto.article.template;

/**
 * 文章模板统计DTO
 * 用于countArticles和countTemplates方法的参数
 */
import java.util.List;

public class ArticleTemplateCountDTO {
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 状态列表（多个状态）
     */
    private List<String> statusList;
    
    /**
     * 关键词
     */
    private String keyword;
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public List<String> getStatusList() {
        return statusList;
    }
    
    public void setStatusList(List<String> statusList) {
        this.statusList = statusList;
    }
    
    public String getKeyword() {
        return keyword;
    }
    
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}