package com.huoli.hlai.generate.article.model.dto.article.template;

/**
 * 文章查询DTO
 * 用于文章列表查询的参数
 */
import java.util.List;

public class ArticleQueryDTO {
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
    
    /**
     * 偏移量
     */
    private int offset;
    
    /**
     * 限制数
     */
    private int limit;
    
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
    
    public int getOffset() {
        return offset;
    }
    
    public void setOffset(int offset) {
        this.offset = offset;
    }
    
    public int getLimit() {
        return limit;
    }
    
    public void setLimit(int limit) {
        this.limit = limit;
    }
}