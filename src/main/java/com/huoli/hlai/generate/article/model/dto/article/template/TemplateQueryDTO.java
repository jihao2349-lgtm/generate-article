package com.huoli.hlai.generate.article.model.dto.article.template;

/**
 * 模板查询DTO
 * 用于模板列表查询的参数
 */
public class TemplateQueryDTO {
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 状态
     */
    private String status;
    
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
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