package com.huoli.hlai.generate.article.model.vo.article.template;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;
import java.util.List;

/**
 * 文章VO
 * 用于文章列表查询的返回数据
 */
@Schema(name = "ArticleListVO", description = "文章信息")
public class ArticleListVO
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

	/**
	 * 创建时间
	 */
	@Schema(description = "创建时间")
	private Date createTime;

	/**
	 * 文章标签列表
	 */
	@Schema(description = "文章标签列表")
	private List<String> contentTagList;

	/**
	 * 篇幅长度
	 */
	@Schema(description = "篇幅长度")
	private String contentLength;

	/**
	 * 文章状态
	 */
	@Schema(description = "文章状态")
	private String status;

	// getter和setter方法
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

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public List<String> getContentTagList() {
		return contentTagList;
	}

	public void setContentTagList(List<String> contentTagList) {
		this.contentTagList = contentTagList;
	}

	public String getContentLength() {
		return contentLength;
	}

	public void setContentLength(String contentLength) {
		this.contentLength = contentLength;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}