package com.huoli.hlai.generate.article.model.entity;

import java.util.Date;

/**
 * 文章模板实体类
 */
public class ArticleTemplate
{

	/**
	 * 文章ID（主键，使用UUID）
	 */
	private String articleTemplateId;

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 文章标题
	 */
	private String articleTitle;

	/**
	 * 模板标题（被收藏为模板时使用）
	 */
	private String templateTitle;

	/**
	 * 文件类型（如txt, docx, pdf等）
	 */
	private String fileType;

	/**
	 * 文件存储路径
	 */
	private String filePath;

	/**
	 * 文章链接（URL导入时使用）
	 */
	private String importUrl;

	/**
	 * 文章状态（awaitingParsing, parsing, parsingFailed, parsed, favorited, deleted）
	 */
	private String status;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 更新时间
	 */
	private Date updateTime;

	/**
	 * 文章内容摘要（拆解成功后才有）
	 */
	private String summary;

	/**
	 * 文章标签（拆解成功后才有，JSON格式）
	 */
	private String tags;

	/**
	 * 篇幅长度
	 */
	private String contentLength;

	public String getArticleTemplateId() {
		return articleTemplateId;
	}

	public void setArticleTemplateId(String articleTemplateId) {
		this.articleTemplateId = articleTemplateId;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public String getArticleTitle() {
		return articleTitle;
	}

	public void setArticleTitle(String articleTitle) {
		this.articleTitle = articleTitle;
	}

	public String getTemplateTitle() {
		return templateTitle;
	}

	public void setTemplateTitle(String templateTitle) {
		this.templateTitle = templateTitle;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getImportUrl()
	{
		return importUrl;
	}

	public void setImportUrl(String importUrl)
	{
		this.importUrl = importUrl;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getContentLength() {
		return contentLength;
	}

	public void setContentLength(String contentLength) {
		this.contentLength = contentLength;
	}
}