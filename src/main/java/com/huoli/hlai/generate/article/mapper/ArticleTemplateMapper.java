package com.huoli.hlai.generate.article.mapper;

import com.huoli.hlai.generate.article.model.dto.article.template.ArticleQueryDTO;
import com.huoli.hlai.generate.article.model.dto.article.template.ArticleTemplateCountDTO;
import com.huoli.hlai.generate.article.model.dto.article.template.TemplateQueryDTO;
import com.huoli.hlai.generate.article.model.entity.ArticleTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章模板Mapper接口
 */
@Mapper
public interface ArticleTemplateMapper
{

	/**
	 * 插入文章
	 * @param articleTemplate 文章实体
	 * @return 影响行数
	 */
	int insert(ArticleTemplate articleTemplate);

	/**
	 * 批量插入文章
	 * @param articleTemplates 文章列表
	 * @return 影响行数
	 */
	int batchInsert(List<ArticleTemplate> articleTemplates);

	/**
	 * 更新文章
	 * @param articleTemplate 文章实体
	 * @return 影响行数
	 */
	int update(ArticleTemplate articleTemplate);

	/**
	 * 根据ID和用户ID查询文章
	 * @param articleTemplateId 文章模板ID
	 * @param userId 用户ID
	 * @return 文章实体
	 */
	ArticleTemplate selectById(@Param("articleTemplateId") String articleTemplateId, @Param("userId") String userId);

	/**
	 * 根据状态查询文章列表
	 * @param status 状态
	 * @param userId 用户ID
	 * @return 文章列表
	 */
	List<ArticleTemplate> selectByStatus(@Param("status") String status, @Param("userId") String userId);

	/**
	 * 根据关键词和用户ID分页查询文章列表
	 * @param queryDTO 文章查询参数
	 * @return 文章列表
	 */
	List<ArticleTemplate> selectArticlesWithPagination(ArticleQueryDTO queryDTO);

	/**
	 * 根据关键词和用户ID查询文章总数
	 * @param countDTO 文章统计参数
	 * @return 文章总数
	 */
	int countArticles(ArticleTemplateCountDTO countDTO);

	/**
	 * 分页查询模板列表
	 * @param queryDTO 模板查询参数
	 * @return 模板列表
	 */
	List<ArticleTemplate> selectTemplatesWithPagination(TemplateQueryDTO queryDTO);

	/**
	 * 查询模板总数
	 * @param countDTO 文章统计参数
	 * @return 模板总数
	 */
	int countTemplates(ArticleTemplateCountDTO countDTO);
}