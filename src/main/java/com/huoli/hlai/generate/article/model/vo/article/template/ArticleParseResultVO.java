package com.huoli.hlai.generate.article.model.vo.article.template;

import com.huoli.hlai.generate.article.model.dto.article.template.ArticleTemplateTagDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;
import java.util.List;

/**
 * 文章解析结果VO
 * 用于表示文章智能拆解的结果信息
 */
@Schema(name = "ArticleParseResultVO", description = "文章智能拆解结果信息")
public class ArticleParseResultVO
{
    
    /**
     * 文章标题
     */
    @Schema(description = "文章标题")
    private String articleTitle;
    
    /**
     * 文件类型
     */
    @Schema(description = "文件类型")
    private String fileType;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;
    
    /**
     * 状态
     */
    @Schema(description = "状态")
    private String status;
    
    /**
     * 摘要
     */
    @Schema(description = "摘要")
    private String summary;
    
    /**
     * 标签列表
     */
    @Schema(description = "标签列表")
    private List<ArticleTemplateTagDTO> tagList;
    
    public String getArticleTitle() {
        return articleTitle;
    }

    public void setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public Date getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    public List<ArticleTemplateTagDTO> getTagList() {
        return tagList;
    }

    public void setTagList(List<ArticleTemplateTagDTO> tagList) {
        this.tagList = tagList;
    }
}