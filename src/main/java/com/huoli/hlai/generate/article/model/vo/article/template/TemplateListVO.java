package com.huoli.hlai.generate.article.model.vo.article.template;

import com.huoli.hlai.generate.article.model.dto.article.template.ArticleTemplateTagDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 模板VO
 * 用于模板列表查询的返回数据
 */
@Schema(name = "TemplateListVO", description = "模板信息")
public class TemplateListVO
{
	/**
	 * 文章模板ID
	 */
	@Schema(description = "文章模板ID")
	private String articleTemplateId;

	/**
	 * 模板标题
	 */
	@Schema(description = "模板标题")
	private String templateTitle;

	/**
	 * 文章摘要
	 */
	@Schema(description = "文章摘要")
	private String summary;

	/**
	 * 文章标签列表
	 */
	@Schema(description = "文章标签列表")
	private List<ArticleTemplateTagDTO> tagList;

	/**
	 * 文件类型
	 */
	@Schema(description = "文件类型")
	private String fileType;

	// getter和setter方法
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

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
}