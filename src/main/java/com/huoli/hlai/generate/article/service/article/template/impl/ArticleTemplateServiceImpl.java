package com.huoli.hlai.generate.article.service.article.template.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huoli.hlai.generate.article.context.UserContext;
import com.huoli.hlai.generate.article.exception.BusinessException;
import com.huoli.hlai.generate.article.exception.ErrorCode;
import com.huoli.hlai.generate.article.mapper.ArticleTemplateMapper;
import com.huoli.hlai.generate.article.model.dto.PageResult;
import com.huoli.hlai.generate.article.model.dto.PaginationQueryDTO;
import com.huoli.hlai.generate.article.model.dto.article.template.ArticleQueryDTO;
import com.huoli.hlai.generate.article.model.dto.article.template.ArticleTemplateCountDTO;
import com.huoli.hlai.generate.article.model.dto.article.template.ArticleTemplateTagDTO;
import com.huoli.hlai.generate.article.model.dto.article.template.TemplateQueryDTO;
import com.huoli.hlai.generate.article.model.entity.ArticleTemplate;
import com.huoli.hlai.generate.article.model.enums.ArticleTemplateStatus;
import com.huoli.hlai.generate.article.model.vo.article.template.*;
import com.huoli.hlai.generate.article.service.article.template.ArticleParserService;
import com.huoli.hlai.generate.article.service.article.template.ArticleTemplateService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * 文章模板服务实现
 */
@Service
public class ArticleTemplateServiceImpl implements ArticleTemplateService
{

	private static final Logger logger = LoggerFactory.getLogger(ArticleTemplateServiceImpl.class);

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private ArticleTemplateMapper articleTemplateMapper;

	@Autowired
	@Qualifier("articleParseExecutor")
	private Executor articleParseExecutor;

	/**
	 * 文件上传路径
	 */
	@Value("${file.upload.path}")
	private String uploadPath;

	/**
	 * 文件上传最大大小
	 */
	@Value("${file.upload.max-size}")
	private String maxFileSize;

	/**
	 * 允许的文件类型
	 */
	@Value("${file.upload.allowed-types}")
	private String allowedTypes;

	@Autowired
	private ArticleParserService articleParserService;

	@Override
	public void uploadArticles(List<MultipartFile> files) {
		logger.info("开始上传文章，文件数量：{}", files != null ? files.size() : 0);
		long startTime = System.currentTimeMillis();
		try {
			// 获取用户 ID
			String userId = UserContext.getUserId();
			logger.debug("用户 ID: {}", userId);

			// 验证文件大小
			long maxSize = parseFileSize(maxFileSize);
			// 验证文件类型
			Set<String> allowedTypeSet = new HashSet<>(Arrays.asList(allowedTypes.split(",")));

			// 批量处理文件
			List<ArticleTemplate> articleTemplates = Collections.synchronizedList(new ArrayList<>(files.size()));
			
			// 并行处理文件上传
			CompletableFuture<?>[] futures = files.stream()
				.map(file -> CompletableFuture.runAsync(() -> {
					String fileName = file.getOriginalFilename();
					logger.debug("处理文件：{}", fileName);

					// 验证文件大小
					if (file.getSize() > maxSize) {
						logger.warn("文件大小超过限制：{} ({}), 最大限制：{}", fileName, file.getSize(), maxFileSize);
						throw new BusinessException(ErrorCode.BUSINESS_ERROR, "文件大小不能超过" + maxFileSize);
					}

					// 验证文件类型
					String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
					if (!allowedTypeSet.contains(fileExtension)) {
						logger.warn("不支持的文件类型：{} ({}), 支持的类型：{}", fileName, fileExtension, allowedTypes);
						throw new BusinessException(ErrorCode.BUSINESS_ERROR, "只支持以下文件类型：" + allowedTypes);
					}

					// 生成唯一文件名
					String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName.replaceAll("[\\/:*?\"<>|]", "_");
					// 安全构建文件路径，防止路径遍历攻击
					Path uploadDir = Paths.get(uploadPath);
					Path destPath = uploadDir.resolve(uniqueFileName).normalize();
					// 存储文件
					File dest = destPath.toFile();
					if (!dest.getParentFile().exists()) {
						logger.debug("创建目录：{}", dest.getParentFile().getPath());
						dest.getParentFile().mkdirs();
					}
					try {
						file.transferTo(dest);
					} catch (IOException e) {
						logger.error("文件存储失败：{}", fileName, e);
						throw new BusinessException("文件存储失败：");
					}
					String filePath = destPath.toString();
					logger.debug("文件存储成功：{}", filePath);

					// 创建文章实体
					ArticleTemplate articleTemplate = new ArticleTemplate();
					articleTemplate.setArticleTemplateId(UUID.randomUUID().toString());
					articleTemplate.setArticleTitle(fileName);
					articleTemplate.setFileType(fileExtension);
					articleTemplate.setFilePath(filePath);
					articleTemplate.setStatus(ArticleTemplateStatus.AWAITING_PARSING.getValue());
					articleTemplate.setUserId(userId);
					articleTemplate.setCreateTime(new Date());
					articleTemplate.setUpdateTime(new Date());

					// 添加到批量处理列表
					articleTemplates.add(articleTemplate);
				}, articleParseExecutor))
				.toArray(CompletableFuture[]::new);

			// 等待所有文件处理完成
			CompletableFuture.allOf(futures).join();

			// 批量保存到数据库
			if (!articleTemplates.isEmpty()) {
				long dbStartTime = System.currentTimeMillis();
				articleTemplateMapper.batchInsert(articleTemplates);
				logger.info("批量保存文章模板成功，数量：{}, 耗时：{}ms", articleTemplates.size(), (System.currentTimeMillis() - dbStartTime));
			}

			long totalTime = System.currentTimeMillis() - startTime;
			logger.info("文章上传完成，成功处理 {} 个文件，总耗时：{}ms", files.size(), totalTime);
		} catch (Exception e) {
			logger.error("文件上传失败，用户 ID: {}, 文件数量：{}", UserContext.getUserId(), files.size(), e);
			throw new BusinessException(ErrorCode.BUSINESS_ERROR, "上传失败");
		}
	}

	@Override
	public void importArticles(List<String> urlList) {
		logger.info("开始导入文章，URL 数量：{}", urlList != null ? urlList.size() : 0);
		long startTime = System.currentTimeMillis();
		try {
			// 获取用户 ID
			String userId = UserContext.getUserId();
			logger.debug("用户 ID: {}", userId);

			// 创建文章实体并批量保存到数据库
			List<ArticleTemplate> articleTemplates = new ArrayList<>();
			int skippedCount = 0;
			for (String url : urlList) {
				if (url == null || url.isEmpty())
				{
					logger.debug("跳过空 URL");
					skippedCount++;
					continue;
				}
				if (url.length() > 500)
				{
					logger.warn("URL 长度超过 500 个字符，跳过：{}", url.substring(0, Math.min(100, url.length())));
					skippedCount++;
					continue;
				}
				// 创建文章实体
				ArticleTemplate articleTemplate = new ArticleTemplate();
				articleTemplate.setArticleTemplateId(UUID.randomUUID().toString());
				articleTemplate.setArticleTitle("url 导入成功，智能拆解中：" + url);
				articleTemplate.setImportUrl(url);
				articleTemplate.setStatus(ArticleTemplateStatus.PARSING.getValue());
				articleTemplate.setUserId(userId);
				articleTemplate.setCreateTime(new Date());
				articleTemplate.setUpdateTime(new Date());

				articleTemplates.add(articleTemplate);
			}

			// 批量保存到数据库
			if (!articleTemplates.isEmpty()) {
				long dbStartTime = System.currentTimeMillis();
				articleTemplateMapper.batchInsert(articleTemplates);
				logger.info("批量保存 URL 导入记录成功，数量：{}, 耗时：{}ms", articleTemplates.size(), (System.currentTimeMillis() - dbStartTime));
			}

			// 启动异步解析任务
			if (!articleTemplates.isEmpty()) {
				CompletableFuture.runAsync(() -> articleParserService.parseUrl(articleTemplates), articleParseExecutor)
						.orTimeout(120, TimeUnit.SECONDS)
						.whenComplete((result, ex) -> {
							if (ex != null) {
								logger.error("文章 URL 智能拆解任务执行异常，URL 数量：{}", articleTemplates.size(), ex);
								for (ArticleTemplate template : articleTemplates) {
									try {
										// 查询最新的文章状态
										ArticleTemplate currentTemplate = articleTemplateMapper.selectById(template.getArticleTemplateId(), template.getUserId());
										if (currentTemplate != null && ArticleTemplateStatus.PARSING.getValue().equals(currentTemplate.getStatus())) {
											// 仅当状态仍为解析中时更新为解析失败
											currentTemplate.setStatus(ArticleTemplateStatus.PARSING_FAILED.getValue());
											currentTemplate.setUpdateTime(new Date());
											articleTemplateMapper.update(currentTemplate);
											logger.info("文章状态更新为解析失败，文章 ID: {}", template.getArticleTemplateId());
										}
									} catch (Exception e) {
										logger.error("更新文章状态失败，文章 ID: {}", template.getArticleTemplateId(), e);
									}
								}
							} else {
								logger.info("文章 URL 智能拆解任务执行成功，URL 数量：{}", articleTemplates.size());
							}
						});
				logger.info("文章 URL 智能拆解任务已提交异步执行，URL 数量：{}", articleTemplates.size());
			}

			long totalTime = System.currentTimeMillis() - startTime;
			logger.info("URL 导入完成，成功处理 {} 个 URL，跳过 {} 个，总耗时：{}ms", articleTemplates.size(), skippedCount, totalTime);
		} catch (Exception e) {
			logger.error("URL 导入失败，用户 ID: {}", UserContext.getUserId(), e);
			throw new BusinessException(ErrorCode.BUSINESS_ERROR, "导入失败：" + e.getMessage());
		}
	}

	@Override
	public List<ArticleAwaitingParsingVO> getAwaitingParsingArticles() {
		// 获取用户ID
		String userId = UserContext.getUserId();

		List<ArticleTemplate> articleTemplates = articleTemplateMapper.selectByStatus(ArticleTemplateStatus.AWAITING_PARSING.getValue(), userId);
		List<ArticleAwaitingParsingVO> result = new ArrayList<>();
		for (ArticleTemplate articleTemplate : articleTemplates) {
			ArticleAwaitingParsingVO vo = new ArticleAwaitingParsingVO();
			vo.setArticleTemplateId(articleTemplate.getArticleTemplateId());
			vo.setArticleTitle(articleTemplate.getArticleTitle());
			result.add(vo);
		}
		return result;
	}

	@Override
	public void parseArticle(String articleTemplateId) {
		logger.info("开始解析文章，文章 ID: {}", articleTemplateId);
		long startTime = System.currentTimeMillis();
		try {
			// 获取用户 ID
			String userId = UserContext.getUserId();
			logger.debug("用户 ID: {}", userId);

			// 查询文章
			ArticleTemplate articleTemplate = getUserArticleTemplate(articleTemplateId, userId);

			// 更新状态为解析中
			articleTemplate.setStatus(ArticleTemplateStatus.PARSING.getValue());
			articleTemplate.setUpdateTime(new Date());
			articleTemplateMapper.update(articleTemplate);
			logger.debug("文章状态更新为解析中，文章 ID: {}", articleTemplateId);

			// 使用 CompletableFuture 异步执行解析任务
			CompletableFuture.runAsync(() -> articleParserService.parseArticle(articleTemplateId, userId), articleParseExecutor)
				.orTimeout(120, TimeUnit.SECONDS)
				.whenComplete((result, ex) -> {
					if (ex != null) {
						logger.error("解析任务执行异常，文章 ID: {}", articleTemplateId, ex);
						try {
							ArticleTemplate failedTemplate = articleTemplateMapper.selectById(articleTemplateId, userId);
							if (failedTemplate != null) {
								failedTemplate.setStatus(ArticleTemplateStatus.PARSING_FAILED.getValue());
								failedTemplate.setUpdateTime(new Date());
								articleTemplateMapper.update(failedTemplate);
							}
						} catch (Exception e) {
							logger.error("更新解析失败状态失败，文章 ID: {}", articleTemplateId, e);
						}
					} else {
						logger.info("解析任务执行成功，文章 ID: {}", articleTemplateId);
					}
				});
			long totalTime = System.currentTimeMillis() - startTime;
			logger.info("解析任务已提交异步执行，文章 ID: {}, 耗时：{}ms", articleTemplateId, totalTime);
		} catch (Exception e) {
			logger.error("解析任务提交失败，文章 ID: {}", articleTemplateId, e);
			throw new BusinessException(ErrorCode.BUSINESS_ERROR, "提交任务失败：" + e.getMessage());
		}
	}

	/**
	 * 解析文件大小字符串为字节数
	 * @param sizeStr 文件大小字符串，如 "10MB"
	 * @return 字节数
	 */
	private long parseFileSize(String sizeStr) {
		long size = Long.parseLong(sizeStr.replaceAll("[^0-9]", ""));
		if (sizeStr.toUpperCase().contains("MB")) {
			size *= 1024 * 1024;
		} else if (sizeStr.toUpperCase().contains("KB")) {
			size *= 1024;
		} else if (sizeStr.toUpperCase().contains("GB")) {
			size *= 1024 * 1024 * 1024;
		}
		return size;
	}

	@Override
	public ArticleParseResultVO getParseResult(String articleTemplateId) {
		logger.debug("获取解析结果，文章 ID: {}", articleTemplateId);
		try {
			// 获取用户 ID
			String userId = UserContext.getUserId();
			logger.debug("用户 ID: {}", userId);

			// 查询文章
			ArticleTemplate articleTemplate = getUserArticleTemplate(articleTemplateId, userId);

			// 构建响应数据
			ArticleParseResultVO data = new ArticleParseResultVO();
			data.setArticleTitle(articleTemplate.getArticleTitle());
			data.setFileType(articleTemplate.getFileType());
			data.setCreateTime(articleTemplate.getCreateTime());
			data.setStatus(articleTemplate.getStatus());
			data.setSummary(articleTemplate.getSummary());
			// 处理标签，解析为 List<ArticleTemplateTagDTO>
			data.setTagList(parseTags(articleTemplate.getTags()));

			logger.info("解析结果获取成功，文章 ID: {}, 状态：{}", articleTemplateId, data.getStatus());
			return data;
		} catch (Exception e) {
			logger.error("获取解析结果失败，文章 ID: {}", articleTemplateId, e);
			throw new BusinessException(ErrorCode.BUSINESS_ERROR, "查询失败");
		}
	}

	@Override
	public PageResult<ArticleListVO> getArticleList(PaginationQueryDTO dto) {
		logger.debug("获取文章列表，关键词：{}, 页码：{}, 每页大小：{}", dto.getKeyword(), dto.getPageNum(), dto.getPageSize());
		try {
			// 获取用户 ID
			String userId = UserContext.getUserId();
			logger.debug("用户 ID: {}", userId);

			// 构建查询参数
			ArticleQueryDTO queryDTO = new ArticleQueryDTO();
			queryDTO.setUserId(userId);
			// 设置状态列表，同时查询已解析和已收藏状态
			List<String> statusList = new ArrayList<>();
			statusList.add(ArticleTemplateStatus.PARSED.getValue());
			statusList.add(ArticleTemplateStatus.FAVORITED.getValue());
			queryDTO.setStatusList(statusList);
			queryDTO.setKeyword(dto.getKeyword());
			int offset = (dto.getPageNum() - 1) * dto.getPageSize();
			queryDTO.setOffset(offset);
			queryDTO.setLimit(dto.getPageSize());

			// 构建统计参数
			ArticleTemplateCountDTO countDTO = new ArticleTemplateCountDTO();
			countDTO.setUserId(userId);
			// 设置状态列表，同时统计已解析和已收藏状态
			countDTO.setStatusList(statusList);
			countDTO.setKeyword(dto.getKeyword());

			// 计算总数
			int total = articleTemplateMapper.countArticles(countDTO);
			logger.debug("文章总数：{}", total);

			// 查询数据
			List<ArticleTemplate> articleTemplates = articleTemplateMapper.selectArticlesWithPagination(queryDTO);
			logger.debug("查询到文章数量：{}", articleTemplates.size());

			// 构建响应数据
			List<ArticleListVO> articleList = new ArrayList<>();

			for (ArticleTemplate articleTemplate : articleTemplates) {
				ArticleListVO vo = new ArticleListVO();
				vo.setArticleTemplateId(articleTemplate.getArticleTemplateId());
				vo.setArticleTitle(articleTemplate.getArticleTitle());
				vo.setCreateTime(articleTemplate.getCreateTime());

				// 处理标签，提取 value 字段
				vo.setContentTagList(extractTagValues(articleTemplate.getTags()));

				vo.setContentLength(articleTemplate.getContentLength());
				vo.setStatus(articleTemplate.getStatus());
				articleList.add(vo);
			}

			// 创建分页结果对象
			PageResult<ArticleListVO> result = new PageResult<>(total, dto.getPageNum(), dto.getPageSize(), articleList);
			logger.info("文章列表获取成功，总数: {}, 分页: {}/{}", total, dto.getPageNum(), (total + dto.getPageSize() - 1) / dto.getPageSize());
			return result;
		} catch (Exception e) {
			logger.error("获取文章列表失败，用户 ID: {}", UserContext.getUserId(), e);
			// 发生异常时返回空结果
			return new PageResult<>(0, dto.getPageNum(), dto.getPageSize(), new ArrayList<>());
		}
	}

	@Override
	public ArticleDetailVO getArticleDetail(String articleTemplateId, HttpServletRequest request) {
		logger.debug("获取文章详情，文章 ID: {}", articleTemplateId);
		try {
			// 获取用户 ID
			String userId = UserContext.getUserId();
			logger.debug("用户 ID: {}", userId);

			// 查询文章
			ArticleTemplate articleTemplate = getUserArticleTemplate(articleTemplateId, userId);

			// 构建响应数据
			ArticleDetailVO detailVO = new ArticleDetailVO();

			// 判断文章来源
			if (articleTemplate.getImportUrl() != null && !articleTemplate.getImportUrl().isEmpty()) {
				// URL 导入的文章
				detailVO.setArticleTemplateSource("url");
				detailVO.setArticleTemplateUrl(articleTemplate.getImportUrl());
				logger.debug("文章来源：URL, 地址：{}", articleTemplate.getImportUrl());
			} else if (articleTemplate.getFilePath() != null && !articleTemplate.getFilePath().isEmpty()) {
				// 文件上传的文章：生成完整的下载链接
				detailVO.setArticleTemplateSource("file");
				// 构建完整的基础 URL
				String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
				String downloadUrl = baseUrl + "/api/article-template/articles/download/" + userId + "/" + articleTemplateId;
				detailVO.setArticleTemplateUrl(downloadUrl);
				logger.debug("文章来源：文件，下载链接：{}", downloadUrl);
			}

			logger.info("文章详情获取成功，文章 ID: {}", articleTemplateId);
			return detailVO;
		} catch (Exception e) {
			logger.error("获取文章详情失败，文章 ID: {}", articleTemplateId, e);
			throw new BusinessException(ErrorCode.BUSINESS_ERROR, "查询失败：" + e.getMessage());
		}
	}

	@Override
	public void downloadArticleFile(String articleTemplateId, HttpServletResponse response) {
		logger.info("下载文件，文章 ID: {}", articleTemplateId);
		try {
			// 获取用户 ID
			String userId = UserContext.getUserId();
			logger.debug("用户 ID: {}", userId);

			// 查询文章
			ArticleTemplate articleTemplate = getUserArticleTemplate(articleTemplateId, userId);

			String filePath = articleTemplate.getFilePath();
			if (filePath == null || filePath.isEmpty()) {
				logger.warn("文件路径为空，文章 ID: {}", articleTemplateId);
				throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "文件不存在");
			}

			// 安全验证文件路径
			Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
			Path fileToDownload = Paths.get(filePath).toAbsolutePath().normalize();
			// 确保下载的文件在上传目录内
			if (!fileToDownload.startsWith(uploadDir)) {
				logger.warn("非法的文件路径：{}", filePath);
				throw new BusinessException(ErrorCode.BUSINESS_ERROR, "无效的文件路径");
			}

			// 检查文件是否存在
			File file = new File(filePath);
			if (!file.exists()) {
				logger.warn("文件不存在，路径：{}", filePath);
				throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "文件不存在");
			}
			// 确保是文件而不是目录
			if (!file.isFile()) {
				logger.warn("路径不是文件：{}", filePath);
				throw new BusinessException(ErrorCode.BUSINESS_ERROR, "无效的文件路径");
			}

			logger.debug("开始下载文件，路径：{}, 大小：{}", filePath, file.length());
			// 设置响应头
			response.setContentType("application/octet-stream");
			response.setContentLength((int) file.length());
			response.setHeader("Content-Disposition", "attachment; filename=" + java.net.URLEncoder.encode(articleTemplate.getArticleTitle(), "UTF-8"));

			// 输出文件流
			try (java.io.FileInputStream fis = new java.io.FileInputStream(file);
				 java.io.BufferedInputStream bis = new java.io.BufferedInputStream(fis);
				 java.io.OutputStream os = response.getOutputStream()) {
				byte[] buffer = new byte[4096];
				int len;
				while ((len = bis.read(buffer)) > 0) {
					os.write(buffer, 0, len);
					os.flush();
				}
			}
			logger.info("文件下载成功，文章 ID: {}", articleTemplateId);
		} catch (Exception e) {
			logger.error("文件下载失败，文章 ID: {}", articleTemplateId, e);
			throw new BusinessException(ErrorCode.BUSINESS_ERROR, "下载失败");
		}
	}

	@Override
	public void favoriteArticle(String articleTemplateId) {
		logger.info("收藏文章，文章 ID: {}", articleTemplateId);
		try {
			// 获取用户 ID
			String userId = UserContext.getUserId();
			logger.debug("用户 ID: {}", userId);

			// 查询文章
			ArticleTemplate articleTemplate = getUserArticleTemplate(articleTemplateId, userId);

			// 更新状态为已收藏
			articleTemplate.setStatus(ArticleTemplateStatus.FAVORITED.getValue());
			articleTemplate.setTemplateTitle(articleTemplate.getArticleTitle());
			articleTemplate.setUpdateTime(new Date());
			articleTemplateMapper.update(articleTemplate);
			logger.info("文章收藏成功，文章 ID: {}", articleTemplateId);
		} catch (Exception e) {
			logger.error("文章收藏失败，文章 ID: {}", articleTemplateId, e);
			throw new BusinessException(ErrorCode.BUSINESS_ERROR, "收藏失败");
		}
	}

	@Override
	public void cancelFavoriteArticle(String articleTemplateId) {
		logger.info("取消收藏文章，文章 ID: {}", articleTemplateId);
		try {
			// 获取用户 ID
			String userId = UserContext.getUserId();
			logger.debug("用户 ID: {}", userId);

			// 查询文章
			ArticleTemplate articleTemplate = getUserArticleTemplate(articleTemplateId, userId);

			// 更新状态为已拆解
			articleTemplate.setStatus(ArticleTemplateStatus.PARSED.getValue());
			articleTemplate.setTemplateTitle(null);
			articleTemplate.setUpdateTime(new Date());
			articleTemplateMapper.update(articleTemplate);
			logger.info("取消收藏成功，文章 ID: {}", articleTemplateId);
		} catch (Exception e) {
			logger.error("取消收藏失败，文章 ID: {}", articleTemplateId, e);
			throw new BusinessException(ErrorCode.BUSINESS_ERROR, "取消收藏失败");
		}
	}

	@Override
	public void deleteArticle(String articleTemplateId) {
		logger.info("删除文章，文章 ID: {}", articleTemplateId);
		try {
			// 获取用户 ID
			String userId = UserContext.getUserId();
			logger.debug("用户 ID: {}", userId);

			// 查询文章
			ArticleTemplate articleTemplate = getUserArticleTemplate(articleTemplateId, userId);

			// 更新状态为已删除
			articleTemplate.setStatus(ArticleTemplateStatus.DELETED.getValue());
			articleTemplate.setTemplateTitle(null);
			articleTemplate.setUpdateTime(new Date());
			articleTemplateMapper.update(articleTemplate);
			logger.info("文章删除成功，文章 ID: {}", articleTemplateId);
		} catch (Exception e) {
			logger.error("文章删除失败，文章 ID: {}", articleTemplateId, e);
			throw new BusinessException(ErrorCode.BUSINESS_ERROR, "删除失败");
		}
	}

	@Override
	public PageResult<TemplateListVO> getTemplateList(PaginationQueryDTO dto) {
		logger.debug("获取模板列表，关键词：{}, 页码：{}, 每页大小：{}", dto.getKeyword(), dto.getPageNum(), dto.getPageSize());
		try {
			// 获取用户 ID
			String userId = UserContext.getUserId();
			logger.debug("用户 ID: {}", userId);

			// 计算偏移量
			int offset = (dto.getPageNum() - 1) * dto.getPageSize();

			// 构建统计参数
			ArticleTemplateCountDTO countDTO = new ArticleTemplateCountDTO();
			countDTO.setUserId(userId);
			List<String> statusList = new ArrayList<>();
			statusList.add(ArticleTemplateStatus.FAVORITED.getValue());
			countDTO.setStatusList(statusList);
			countDTO.setKeyword(dto.getKeyword());

			// 计算总数
			int total = articleTemplateMapper.countTemplates(countDTO);
			logger.debug("模板总数：{}", total);

			// 构建查询参数
			TemplateQueryDTO queryDTO = new TemplateQueryDTO();
			queryDTO.setUserId(userId);
			queryDTO.setStatus(ArticleTemplateStatus.FAVORITED.getValue());
			queryDTO.setKeyword(dto.getKeyword());
			queryDTO.setOffset(offset);
			queryDTO.setLimit(dto.getPageSize());

			// 查询数据
			List<ArticleTemplate> articleTemplates = articleTemplateMapper.selectTemplatesWithPagination(queryDTO);
			logger.debug("查询到模板数量：{}", articleTemplates.size());

			// 构建响应数据
			List<TemplateListVO> templateList = new ArrayList<>();

			for (ArticleTemplate articleTemplate : articleTemplates) {
				TemplateListVO vo = new TemplateListVO();
				vo.setArticleTemplateId(articleTemplate.getArticleTemplateId());
				vo.setTemplateTitle(articleTemplate.getTemplateTitle());
				vo.setSummary(articleTemplate.getSummary());

				// 处理标签，解析为 List<ArticleTemplateTagDTO>
				vo.setTagList(parseTags(articleTemplate.getTags()));

				vo.setFileType(articleTemplate.getFileType());
				templateList.add(vo);
			}

			// 创建分页结果对象
			PageResult<TemplateListVO> result = new PageResult<>(total, dto.getPageNum(), dto.getPageSize(), templateList);
			logger.info("模板列表获取成功，总数: {}, 分页: {}/{}", total, dto.getPageNum(), (total + dto.getPageSize() - 1) / dto.getPageSize());
			return result;
		} catch (Exception e) {
			logger.error("获取模板列表失败，用户 ID: {}", UserContext.getUserId(), e);
			// 发生异常时返回空结果
			return new PageResult<>(0, dto.getPageNum(), dto.getPageSize(), new ArrayList<>());
		}
	}

	@Override
	public void updateTemplateTitle(String articleTemplateId, String templateTitle) {
		logger.info("更新模板标题，文章 ID: {}, 新标题：{}", articleTemplateId, templateTitle);
		try {
			// 获取用户 ID
			String userId = UserContext.getUserId();
			logger.debug("用户 ID: {}", userId);

			// 查询文章
			ArticleTemplate articleTemplate = getUserArticleTemplate(articleTemplateId, userId);

			// 更新模板标题
			articleTemplate.setTemplateTitle(templateTitle);
			articleTemplate.setUpdateTime(new Date());
			articleTemplateMapper.update(articleTemplate);
			logger.info("模板标题更新成功，文章 ID: {}", articleTemplateId);
		} catch (Exception e) {
			logger.error("更新模板标题失败，文章 ID: {}, 新标题：{}", articleTemplateId, templateTitle, e);
			throw new BusinessException(ErrorCode.BUSINESS_ERROR, "更新失败");
		}
	}

	/**
	 * 获取用户的文章模板
	 * @param articleTemplateId 文章模板ID
	 * @param userId 用户ID
	 * @return 文章模板对象
	 * @throws BusinessException 如果文章不存在
	 */
	private ArticleTemplate getUserArticleTemplate(String articleTemplateId, String userId) {
		ArticleTemplate articleTemplate = articleTemplateMapper.selectById(articleTemplateId, userId);
		if (articleTemplate == null) {
			logger.warn("文章不存在，文章ID: {}", articleTemplateId);
			throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "文章不存在");
		}
		return articleTemplate;
	}

	/**
	 * 解析标签JSON字符串为List<ArticleTemplateTagDTO>
	 * @param tagsJson 标签JSON字符串
	 * @return 标签列表
	 */
	private List<ArticleTemplateTagDTO> parseTags(String tagsJson) {
		if (tagsJson == null || tagsJson.isEmpty()) {
			return new ArrayList<>();
		}
		try {
			// 解析JSON字符串为List<ArticleTemplateTagDTO>
			return objectMapper.readValue(tagsJson,
				objectMapper.getTypeFactory().constructCollectionType(List.class, ArticleTemplateTagDTO.class));
		} catch (JsonProcessingException e) {
			logger.error("标签解析失败", e);
			// 如果解析失败，设置为空列表
			return new ArrayList<>();
		}
	}

	/**
	 * 提取标签值列表
	 * @param tagsJson 标签JSON字符串
	 * @return 标签值列表
	 */
	private List<String> extractTagValues(String tagsJson) {
		List<ArticleTemplateTagDTO> tagList = parseTags(tagsJson);
		List<String> valueList = new ArrayList<>();
		for (ArticleTemplateTagDTO tag : tagList) {
			valueList.add(tag.getValue());
		}
		return valueList;
	}
}