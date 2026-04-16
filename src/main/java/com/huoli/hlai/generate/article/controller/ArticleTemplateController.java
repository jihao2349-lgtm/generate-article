package com.huoli.hlai.generate.article.controller;

import com.huoli.hlai.generate.article.context.UserContext;
import com.huoli.hlai.generate.article.model.Result;
import com.huoli.hlai.generate.article.model.dto.PageResult;
import com.huoli.hlai.generate.article.model.dto.PaginationQueryDTO;
import com.huoli.hlai.generate.article.model.dto.UserInfoDTO;
import com.huoli.hlai.generate.article.model.dto.article.template.ArticleTemplateIdDTO;
import com.huoli.hlai.generate.article.model.dto.article.template.ArticleTemplateTitleUpdateDTO;
import com.huoli.hlai.generate.article.model.dto.article.template.ArticleUploadDTO;
import com.huoli.hlai.generate.article.model.dto.article.template.ArticleUrlImportDTO;
import com.huoli.hlai.generate.article.model.vo.article.template.*;
import com.huoli.hlai.generate.article.service.article.template.ArticleTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章模板控制器
 * 提供文章模板的上传、导入、拆解、查询、收藏等功能
 */
@RestController
@RequestMapping("/api/article-template")
@Tag(name = "article-template", description = "文章模板相关接口")
public class ArticleTemplateController
{

	private static final Logger logger = LoggerFactory.getLogger(ArticleTemplateController.class);

	@Autowired
	private ArticleTemplateService articleTemplateService;

	/**
	 * 文章上传
	 * @param dto 文章上传参数
	 * @return 上传结果
	 */
	@Operation(summary = "文章上传", description = "上传文章文件", operationId = "01_uploadArticles")
	@PostMapping("/articles/upload")
	public Result<Void> uploadArticles(@Parameter(description = "文章上传参数") @Valid ArticleUploadDTO dto) {
		logger.info("接收文章上传请求，用户ID: {}, 文件数量: {}", dto.getUserId(), dto.getFileList().size());
		articleTemplateService.uploadArticles(dto.getFileList());
		logger.info("文章上传成功");
		return Result.success("上传成功");
	}

	/**
	 * 文章链接导入
	 * @param dto 文章导入参数
	 * @return 导入结果
	 */
	@Operation(summary = "文章链接导入", description = "通过URL导入文章", operationId = "02_importArticles")
	@PostMapping("/articles/import")
	public Result<Void> importArticles(@Parameter(description = "文章导入参数") @Valid @RequestBody ArticleUrlImportDTO dto) {
		logger.info("接收文章导入请求，URL数量: {}", dto.getUrlList().size());
		articleTemplateService.importArticles(dto.getUrlList());
		logger.info("文章导入成功");
		return Result.success("导入成功");
	}

	/**
	 * 待拆解文章列表查询
	 * @param dto 用户ID参数
	 * @return 待拆解文章列表
	 */
	@Operation(summary = "待拆解文章列表查询", description = "查询待拆解的文章列表", operationId = "03_getAwaitingParsingArticles")
	@PostMapping("/articles/awaiting-parsing")
	public Result<List<ArticleAwaitingParsingVO>> getAwaitingParsingArticles(@Parameter(description = "用户ID参数") @Valid @RequestBody UserInfoDTO dto) {
		logger.info("接收待拆解文章列表查询请求，用户ID: {}", dto.getUserId());
		List<ArticleAwaitingParsingVO> data = articleTemplateService.getAwaitingParsingArticles();
		logger.info("待拆解文章列表查询成功，数量: {}", data.size());
		return Result.success(data);
	}

	/**
	 * 智能拆解
	 * @param dto 文章模板ID参数
	 * @return 拆解结果
	 */
	@Operation(summary = "智能拆解", description = "智能拆解文章", operationId = "04_parseArticle")
	@PostMapping("/articles/parse")
	public Result<Void> parseArticle(@Parameter(description = "文章模板ID参数") @Valid @RequestBody ArticleTemplateIdDTO dto) {
		logger.info("接收智能拆解请求，文章ID: {}", dto.getArticleTemplateId());
		articleTemplateService.parseArticle(dto.getArticleTemplateId());
		logger.info("智能拆解任务已提交，文章ID: {}", dto.getArticleTemplateId());
		return Result.success("智能拆解任务已提交");
	}

	/**
	 * 智能拆解结果查询
	 * @param dto 文章模板ID参数
	 * @return 拆解结果
	 */
	@Operation(summary = "智能拆解结果查询", description = "查询智能拆解结果", operationId = "05_getParseResult")
	@PostMapping("/articles/parse-result")
	public Result<ArticleParseResultVO> getParseResult(@Parameter(description = "文章模板ID参数") @Valid @RequestBody ArticleTemplateIdDTO dto) {
		logger.info("接收智能拆解结果查询请求，文章ID: {}", dto.getArticleTemplateId());
		ArticleParseResultVO result = articleTemplateService.getParseResult(dto.getArticleTemplateId());
		logger.info("智能拆解结果查询成功，文章ID: {}", dto.getArticleTemplateId());
		return Result.success(result, "查询成功");
	}

	/**
	 * 文章列表查询
	 * @param dto 分页查询参数
	 * @return 文章列表
	 */
	@Operation(summary = "文章列表查询", description = "查询文章列表", operationId = "06_getArticleList")
	@PostMapping("/articles/list")
	public Result<PageResult<ArticleListVO>> getArticleList(@Parameter(description = "分页查询参数") @Valid @RequestBody PaginationQueryDTO dto) {
		logger.info("接收文章列表查询请求，关键词: {}, 页码: {}, 每页大小: {}", dto.getKeyword(), dto.getPageNum(), dto.getPageSize());
		PageResult<ArticleListVO> result = articleTemplateService.getArticleList(dto);
		logger.info("文章列表查询成功，总数: {}", result.getTotal());
		return Result.success(result);
	}

	/**
	 * 文章详情查询
	 * @param dto 文章模板ID参数
	 * @param request HttpServletRequest
	 * @return 文章详情
	 */
	@Operation(summary = "文章详情查询", description = "查询文章详情", operationId = "07_getArticleDetail")
	@PostMapping("/articles/detail")
	public Result<ArticleDetailVO> getArticleDetail(@Parameter(description = "文章模板ID参数") @Valid @RequestBody ArticleTemplateIdDTO dto, HttpServletRequest request) {
		logger.info("接收文章详情查询请求，文章ID: {}", dto.getArticleTemplateId());
		ArticleDetailVO result = articleTemplateService.getArticleDetail(dto.getArticleTemplateId(), request);
		logger.info("文章详情查询成功，文章ID: {}", dto.getArticleTemplateId());
		return Result.success(result, "查询成功");
	}

	/**
	 * 下载文件
	 * @param articleTemplateId 文章模板ID
	 * @param userId 用户ID
	 * @param response HttpServletResponse
	 */
	@Operation(summary = "文章文件下载", description = "下载文章文件", operationId = "08_downloadFile")
	@GetMapping("/articles/download/{userId}/{articleTemplateId}")
	public void downloadArticleFile(@Parameter(description = "用户ID") @PathVariable String userId, @Parameter(description = "文章模板ID") @PathVariable String articleTemplateId, HttpServletResponse response) {
		logger.info("接收文章文件下载请求，用户ID: {}, 文章ID: {}", userId, articleTemplateId);
		// 设置用户ID到上下文
		UserContext.setUserId(userId);
		articleTemplateService.downloadArticleFile(articleTemplateId, response);
		logger.info("文章文件下载成功，用户ID: {}, 文章ID: {}", userId, articleTemplateId);
	}

	/**
	 * 收藏文章
	 * @param dto 文章模板ID参数
	 * @return 收藏结果
	 */
	@Operation(summary = "收藏文章", description = "收藏文章", operationId = "09_favoriteArticle")
	@PostMapping("/articles/favorite")
	public Result<Void> favoriteArticle(@Parameter(description = "文章模板ID参数") @Valid @RequestBody ArticleTemplateIdDTO dto) {
		logger.info("接收收藏文章请求，文章ID: {}", dto.getArticleTemplateId());
		articleTemplateService.favoriteArticle(dto.getArticleTemplateId());
		logger.info("文章收藏成功，ID: {}", dto.getArticleTemplateId());
		return Result.success("收藏成功");
	}

	/**
	 * 取消收藏
	 * @param dto 文章模板ID参数
	 * @return 取消收藏结果
	 */
	@Operation(summary = "取消收藏", description = "取消收藏文章", operationId = "10_cancelFavoriteArticle")
	@PostMapping("/articles/cancel-favorite")
	public Result<Void> cancelFavoriteArticle(@Parameter(description = "文章模板ID参数") @Valid @RequestBody ArticleTemplateIdDTO dto) {
		logger.info("接收取消收藏请求，文章ID: {}", dto.getArticleTemplateId());
		articleTemplateService.cancelFavoriteArticle(dto.getArticleTemplateId());
		logger.info("取消收藏成功，ID: {}", dto.getArticleTemplateId());
		return Result.success("取消收藏成功");
	}

	/**
	 * 删除文章
	 * @param dto 文章模板ID参数
	 * @return 删除结果
	 */
	@Operation(summary = "删除文章", description = "删除文章", operationId = "11_deleteArticle")
	@PostMapping("/articles/delete")
	public Result<Void> deleteArticle(@Parameter(description = "文章模板ID参数") @Valid @RequestBody ArticleTemplateIdDTO dto) {
		logger.info("接收删除文章请求，文章ID: {}", dto.getArticleTemplateId());
		articleTemplateService.deleteArticle(dto.getArticleTemplateId());
		logger.info("文章删除成功，ID: {}", dto.getArticleTemplateId());
		return Result.success("删除成功");
	}

	/**
	 * 文章模板列表查询
	 * @param dto 分页查询参数
	 * @return 模板列表
	 */
	@Operation(summary = "文章模板列表查询", description = "查询文章模板列表", operationId = "12_getTemplateList")
	@PostMapping("/templates/list")
	public Result<PageResult<TemplateListVO>> getTemplateList(@Parameter(description = "分页查询参数") @Valid @RequestBody PaginationQueryDTO dto) {
		logger.info("接收模板列表查询请求，关键词: {}, 页码: {}, 每页大小: {}", dto.getKeyword(), dto.getPageNum(), dto.getPageSize());
		PageResult<TemplateListVO> result = articleTemplateService.getTemplateList(dto);
		logger.info("模板列表查询成功，总数: {}", result.getTotal());
		return Result.success(result);
	}

	/**
	 * 文章模板标题编辑
	 * @param dto 模板标题更新参数
	 * @return 更新结果
	 */
	@Operation(summary = "文章模板标题编辑", description = "编辑文章模板标题", operationId = "13_updateTemplateTitle")
	@PostMapping("/templates/title/update")
	public Result<Void> updateTemplateTitle(@Parameter(description = "模板标题更新参数") @Valid @RequestBody ArticleTemplateTitleUpdateDTO dto) {
		logger.info("接收模板标题更新请求，ID: {}, 新标题: {}", dto.getArticleTemplateId(), dto.getTemplateTitle());
		articleTemplateService.updateTemplateTitle(dto.getArticleTemplateId(), dto.getTemplateTitle());
		logger.info("模板标题更新成功，ID: {}", dto.getArticleTemplateId());
		return Result.success("更新成功");
	}
}