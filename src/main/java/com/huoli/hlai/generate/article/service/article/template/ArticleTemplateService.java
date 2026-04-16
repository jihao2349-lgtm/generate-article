package com.huoli.hlai.generate.article.service.article.template;

import com.huoli.hlai.generate.article.model.dto.PageResult;
import com.huoli.hlai.generate.article.model.dto.PaginationQueryDTO;
import com.huoli.hlai.generate.article.model.vo.article.template.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文章模板服务接口
 */
public interface ArticleTemplateService
{
	/**
	 * 上传文章
	 * @param files 文件列表
	 */
	void uploadArticles(List<MultipartFile> files);

	/**
	 * 从URL导入文章
	 * @param urlList URL列表
	 */
	void importArticles(List<String> urlList);

	/**
	 * 查询待拆解的文章列表
	 * @return 文章列表
	 */
	List<ArticleAwaitingParsingVO> getAwaitingParsingArticles();

	/**
	 * 智能拆解文章
	 * @param articleTemplateId 文章模板ID
	 */
	void parseArticle(String articleTemplateId);

	/**
	 * 查询智能拆解结果
	 * @param articleTemplateId 文章模板ID
	 * @return 拆解结果
	 */
	ArticleParseResultVO getParseResult(String articleTemplateId);

	/**
	 * 查询文章列表
	 * @param dto 分页查询参数
	 * @return 文章列表
	 */
	PageResult<ArticleListVO> getArticleList(PaginationQueryDTO dto);

	/**
	 * 查询文章详情
	 * @param articleTemplateId 文章模板ID
	 * @param request HttpServletRequest
	 * @return 文章详情
	 */
	ArticleDetailVO getArticleDetail(String articleTemplateId, HttpServletRequest request);
	
	/**
	 * 下载文件
	 * @param articleTemplateId 文章模板ID
	 * @param response HttpServletResponse
	 */
	void downloadArticleFile(String articleTemplateId, HttpServletResponse response);

	/**
	 * 收藏文章
	 * @param articleTemplateId 文章模板ID
	 */
	void favoriteArticle(String articleTemplateId);

	/**
	 * 取消收藏
	 * @param articleTemplateId 文章模板ID
	 */
	void cancelFavoriteArticle(String articleTemplateId);

	/**
	 * 删除文章
	 * @param articleTemplateId 文章模板ID
	 */
	void deleteArticle(String articleTemplateId);

	/**
	 * 查询模板列表
	 * @param dto 分页查询参数
	 * @return 模板列表
	 */
	PageResult<TemplateListVO> getTemplateList(PaginationQueryDTO dto);

	/**
	 * 更新模板标题
	 * @param articleTemplateId 文章模板ID
	 * @param templateTitle 模板标题
	 */
	void updateTemplateTitle(String articleTemplateId, String templateTitle);
}