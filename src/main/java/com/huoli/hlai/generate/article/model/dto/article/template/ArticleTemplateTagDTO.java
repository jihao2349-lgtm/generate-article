package com.huoli.hlai.generate.article.model.dto.article.template;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 文章模板标签DTO
 * 用于表示文章模板的标签信息
 */
@Schema(name = "ArticleTemplateTagDTO", description = "文章模板标签信息")
public class ArticleTemplateTagDTO {
    
    /**
     * 标签类型，content:内容门类、languageStyle:语言风格、structure:结构类型、element:内容元素、color:色值
     */
    @Schema(description = "标签类型")
    private String type;
    
    /**
     * 标签值
     */
    @Schema(description = "标签值")
    private String value;
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
}