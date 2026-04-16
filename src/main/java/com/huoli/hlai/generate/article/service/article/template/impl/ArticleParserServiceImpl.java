package com.huoli.hlai.generate.article.service.article.template.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huoli.hlai.generate.article.exception.BusinessException;
import com.huoli.hlai.generate.article.exception.ErrorCode;
import com.huoli.hlai.generate.article.mapper.ArticleTemplateMapper;
import com.huoli.hlai.generate.article.model.dto.article.template.ArticleTemplateTagDTO;
import com.huoli.hlai.generate.article.model.entity.ArticleTemplate;
import com.huoli.hlai.generate.article.model.enums.ArticleTemplateStatus;
import com.huoli.hlai.generate.article.service.article.template.ArticleParserService;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.files.FileCreateParams;
import com.openai.models.files.FileObject;
import com.openai.models.files.FilePurpose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 文章解析服务实现
 */
@Service
public class ArticleParserServiceImpl implements ArticleParserService {

    private static final Logger logger = LoggerFactory.getLogger(ArticleParserServiceImpl.class);

    @Autowired
    private ArticleTemplateMapper articleTemplateMapper;

    @Value("${spring.ai.dashscope.api-key}")
    private String dashscopeApiKey;

    @Override
    public void parseUrl(List<ArticleTemplate> articleTemplates) {
        logger.info("智能拆解文章URL，数量: {}", articleTemplates.size());
        try {
            // 构建批量请求
            StringBuilder inputBuilder = new StringBuilder();
            for (ArticleTemplate articleTemplate : articleTemplates) {
                inputBuilder.append(articleTemplate.getImportUrl()).append("\n");
            }

            String promptContent = buildUrlPrompt(inputBuilder.toString());

            // 构建请求体
            Map<String, Object> requestBody = Map.of(
                    "model", "qwen3-max",
                    "input", promptContent,
                    "tools", List.of(
                            Map.of("type", "web_search"),
                            Map.of("type", "web_extractor"),
                            Map.of("type", "code_interpreter")
                    ),
                    "enable_thinking", true
            );

            // 创建 WebClient 实例
            WebClient webClient = WebClient.builder()
                    .baseUrl("https://dashscope.aliyuncs.com")
                    .defaultHeader("Authorization", "Bearer " + dashscopeApiKey)
                    .build();
            
            // 使用block()方法同步获取响应
            String response = webClient.post()
                    .uri("/api/v2/apps/protocols/compatible-mode/v1/responses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(java.time.Duration.ofSeconds(120)); // 设置120秒超时
            
            if (response != null) {
                logger.info("AI智能拆解文章URL成功，开始处理返回内容");
                processUrlResponse(response, articleTemplates);
            } else {
                logger.error("AI智能拆解文章URL失败，返回为空");
                // 更新所有文章状态为解析失败
                for (ArticleTemplate articleTemplate : articleTemplates) {
                    updateArticleStatus(articleTemplate, ArticleTemplateStatus.PARSING_FAILED);
                }
            }
        } catch (Exception e) {
            logger.error("智能拆解文章URL Exception", e);
            // 更新所有文章状态为解析失败
            for (ArticleTemplate articleTemplate : articleTemplates) {
                updateArticleStatus(articleTemplate, ArticleTemplateStatus.PARSING_FAILED);
            }
        }
    }

    /**
     * 处理URL解析响应
     * @param response AI响应
     * @param articleTemplates 文章模板列表
     */
    private void processUrlResponse(String response, List<ArticleTemplate> articleTemplates) {
        try {
            // 创建ObjectMapper实例
            ObjectMapper objectMapper = new ObjectMapper();
            
            // 解析JSON字符串为JsonNode
            JsonNode rootNode = objectMapper.readTree(response);
            
            // 获取output数组
            JsonNode outputNode = rootNode.get("output");
            if (outputNode != null && outputNode.isArray()) {
                // 遍历output数组
                for (JsonNode itemNode : outputNode) {
                    // 检查role是否为assistant
                    JsonNode roleNode = itemNode.get("role");
                    if (roleNode != null && "assistant".equals(roleNode.asText())) {
                        // 获取content数组
                        JsonNode contentNode = itemNode.get("content");
                        if (contentNode != null && contentNode.isArray()) {
                            // 遍历content数组
                            for (JsonNode contentItem : contentNode) {
                                // 获取text字段
                                JsonNode textNode = contentItem.get("text");
                                if (textNode != null) {
                                    String text = textNode.asText().trim();
                                    // 处理text内容，提取每个URL的分析结果
                                    processUrlAnalysisResults(text, articleTemplates);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("处理URL响应失败", e);
            // 更新所有文章状态为解析失败
            for (ArticleTemplate articleTemplate : articleTemplates) {
                updateArticleStatus(articleTemplate, ArticleTemplateStatus.PARSING_FAILED);
            }
        }
    }

    /**
     * 处理URL分析结果
     * @param analysisResults 分析结果文本
     * @param articleTemplates 文章模板列表
     */
    private void processUrlAnalysisResults(String analysisResults, List<ArticleTemplate> articleTemplates) {
        try {
            // 1. 解析JSON响应
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(analysisResults);

            // 2. 验证是否为JSON数组
            if (!rootNode.isArray()) {
                logger.error("AI返回的结果不是JSON数组格式");
                for (ArticleTemplate articleTemplate : articleTemplates) {
                    updateArticleStatus(articleTemplate, ArticleTemplateStatus.PARSING_FAILED);
                }
                return;
            }

            // 3. 遍历数组元素
            for (JsonNode itemNode : rootNode) {
                try {
                    // 4. 提取URL
                    String url = extractUrlFromNode(itemNode);
                    if (url == null) {
                        logger.warn("JSON元素缺少url字段");
                        continue;
                    }
                    
                    // 5. 查找对应文章实体
                    ArticleTemplate articleTemplate = findArticleByUrl(url, articleTemplates);
                    if (articleTemplate == null) {
                        logger.warn("未找到对应URL的文章实体: {}", url);
                        continue;
                    }
                    
                    // 6. 提取分析数据并更新文章
                    parseAndUpdateArticle(itemNode, articleTemplate);
                    logger.info("处理AI返回的URL解析结果完成，文章ID: {}", articleTemplate.getArticleTemplateId());
                } catch (Exception e) {
                    logger.error("处理AI返回的URL解析结果Exception", e);
                    // 继续处理下一个元素
                }
            }

            // 7. 标记未处理的文章为失败
            markUnprocessedArticles(articleTemplates);

        } catch (Exception e) {
            logger.error("处理URL分析结果失败", e);
            // 更新所有文章状态为解析失败
            for (ArticleTemplate articleTemplate : articleTemplates) {
                updateArticleStatus(articleTemplate, ArticleTemplateStatus.PARSING_FAILED);
            }
        }
    }

    /**
     * 从JSON节点中提取URL
     * @param node JSON节点
     * @return URL字符串
     */
    private String extractUrlFromNode(JsonNode node) {
        JsonNode urlNode = node.get("url");
        return urlNode != null ? urlNode.asText() : null;
    }

    /**
     * 根据URL查找文章实体
     * @param url URL字符串
     * @param articleTemplates 文章模板列表
     * @return 对应的文章实体
     */
    private ArticleTemplate findArticleByUrl(String url, List<ArticleTemplate> articleTemplates) {
        for (ArticleTemplate article : articleTemplates) {
            if (url.equals(article.getImportUrl())) {
                return article;
            }
        }
        return null;
    }

    /**
     * 标记未处理的文章为解析失败
     * @param articleTemplates 文章模板列表
     */
    private void markUnprocessedArticles(List<ArticleTemplate> articleTemplates) {
        for (ArticleTemplate articleTemplate : articleTemplates) {
            if (ArticleTemplateStatus.AWAITING_PARSING.getValue().equals(articleTemplate.getStatus())) {
                updateArticleStatus(articleTemplate, ArticleTemplateStatus.PARSING_FAILED);
            }
        }
    }

    /**
     * 更新文章状态
     * @param articleTemplate 文章模板
     * @param status 状态
     */
    private void updateArticleStatus(ArticleTemplate articleTemplate, ArticleTemplateStatus status) {
        try {
            articleTemplate.setStatus(status.getValue());
            articleTemplate.setUpdateTime(new Date());
            articleTemplateMapper.update(articleTemplate);
            logger.info("文章状态更新为{}，文章ID: {}", status.name(), articleTemplate.getArticleTemplateId());
        } catch (Exception e) {
            logger.error("更新文章状态失败，文章ID: {}", articleTemplate.getArticleTemplateId(), e);
        }
    }

    @Override
    public void parseArticle(String articleTemplateId, String userId) {
        logger.info("开始智能拆解文章，文章ID: {}", articleTemplateId);
        try {
            ArticleTemplate articleTemplate = articleTemplateMapper.selectById(articleTemplateId, userId);
            if (articleTemplate == null) {
                logger.warn("文章不存在，文章ID: {}", articleTemplateId);
                return;
            }
            if (articleTemplate.getImportUrl() != null && !articleTemplate.getImportUrl().isEmpty()) {
                parseUrl(Collections.singletonList(articleTemplate));
            }
            else if (articleTemplate.getFilePath() != null && !articleTemplate.getFilePath().isEmpty()) {
                parseFile(articleTemplate);
            }
        } catch (Exception e) {
            logger.error("文章智能拆解Exception，文章ID: {}", articleTemplateId, e);
            ArticleTemplate articleTemplate = articleTemplateMapper.selectById(articleTemplateId, userId);
            if (articleTemplate != null) {
                articleTemplate.setStatus(ArticleTemplateStatus.PARSING_FAILED.getValue());
                articleTemplate.setUpdateTime(new Date());
                articleTemplateMapper.update(articleTemplate);
                logger.info("文章状态更新为解析失败，文章ID: {}", articleTemplateId);
            }
        }
    }

    private void parseFile(ArticleTemplate articleTemplate)
    {
        logger.info("智能拆解上传的文件: {}", articleTemplate.getFilePath());
        String promptContent = buildFilePrompt();
        OpenAIClient client = OpenAIOkHttpClient.builder()
                .apiKey(dashscopeApiKey)
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .build();

        Path filePath = Paths.get(articleTemplate.getFilePath());
        FileCreateParams fileParams = FileCreateParams.builder()
                .file(filePath)
                .purpose(FilePurpose.of("file-extract"))
                .build();

        logger.debug("开始上传文件到AI服务");
        FileObject fileObject = client.files().create(fileParams);
        String fileId = fileObject.id();
        logger.debug("文件上传到AI服务成功，FileID: {}", fileId);

        ChatCompletionCreateParams chatParams = ChatCompletionCreateParams.builder()
                .addSystemMessage("fileid://" + fileId)
                .addUserMessage(promptContent)
                .model("qwen-long")
                .build();

        logger.debug("发送AI文章解析请求");
        ChatCompletion chatCompletion = client.chat().completions().create(chatParams);
        String aiResponse = chatCompletion.choices().get(0).message().content().get();
        logger.debug("AI响应文章解析请求成功，开始处理响应内容");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(aiResponse);
            parseAndUpdateArticle(rootNode, articleTemplate);
        } catch (JsonProcessingException e) {
            logger.error("解析AI响应失败", e);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "AI未按要求返回JSON格式的响应: " + e.getMessage());
        }
        logger.info("文章智能拆解完成，文章ID: {}", articleTemplate.getArticleTemplateId());
    }

    /**
     * 基础提示词模板
     */
    private static final String BASE_PROMPT_TEMPLATE = """
        ${URL_INTRO}
        对文档内容进行核心总结陈述，要求：
        1. 提炼文档的核心主题、关键结论及重要数据
        2. 以陈述性语言进行总结，避免主观评价
        3. 总结内容需控制在100字以内，确保信息完整且无冗余
        
        请帮我分析提供的文档的行文风格与排版风格，严格按照以下标签体系输出结果，无需任何额外阐释：
        
        一、行文风格标签（从对应分类中选择匹配项）
        内容门类：福利类、资讯类、规范类、知识科普类、教程指南类、工具资源类、问答解惑类、故事叙事类、观点评论类、治愈陪伴类、趣味娱乐类、种草推荐类
        语言风格：口语化、书面化、网感型、专业型、治愈型、严肃型、活泼型、其他
        结构类型：总分总式、并列式、问答式、叙事式、评论式、清单式、步骤式、其他
        
        二、排版风格标签（从对应分类中选择匹配项）
        内容元素：图文、纯文字、纯图片、纯视频、图文 + 视频、emoji、其他
        推荐排版色值：基于文档内容风格与适用场景，推荐2组核心色值（格式为#RRGGBB,#RRGGBB），分别用于彩色文字强调和高亮区背景
        
        请对该文档的文字部分的篇幅长度进行统计，给出篇幅长度结果：300字以下，300-500字，500-700字，700-1000字，1000字以上
        
        请严格按照以下JSON格式输出分析结果，不得添加任何其他内容：
        {
          ${URL_FIELD}
          "summary": "文档核心总结...",
          "tags": [
            {
              "type": "content",
              "value": "资讯类"
            },
            {
              "type": "languageStyle",
              "value": "书面化"
            },
            {
              "type": "structure",
              "value": "总分总式"
            },
            {
              "type": "element",
              "value": "图文 + 视频"
            },
            {
              "type": "color",
              "value": "#0D6EFD,#FFB702"
            }
          ],
          "contentLength": "500-700字"
        }
        
        ${URL_FOOTER}
        """;

    /**
     * 构建文件解析提示词
     * @return 提示词内容
     */
    private String buildFilePrompt() {
        return BASE_PROMPT_TEMPLATE
                .replace("${URL_INTRO}", "")
                .replace("${URL_FIELD}", "")
                .replace("${URL_FOOTER}", "");
    }

    /**
     * 构建URL解析提示词
     * @param urls URL列表
     * @return 提示词内容
     */
    private String buildUrlPrompt(String urls) {
        String urlIntro = "请逐个访问以下URL链接，对每个链接的内容进行分析，并按要求返回结果：\n\nURL列表：\n" + urls;
        String urlField = "\"url\": \"URL链接\",\n  \"title\": \"文章标题\",";
        String urlFooter = "请返回JSON数组，每个URL链接的解析结果作为JSON数组中的一个元素，每个元素的JSON格式严格按照上述要求进行返回。\n\n特别注意：\n1. 请首先尝试从URL链接中读取原始标题，如果存在则直接使用该标题\n2. 如果URL链接中没有标题或读取不到，请根据文章内容生成一个20字以下的标题\n3. 生成的标题应能准确反映文章的核心内容";
        
        return BASE_PROMPT_TEMPLATE
                .replace("${URL_INTRO}", urlIntro)
                .replace("${URL_FIELD}", urlField)
                .replace("${URL_FOOTER}", urlFooter);
    }

    /**
     * 解析AI响应并更新文章模板
     * @param aiResponse AI响应JsonNode
     * @param articleTemplate 文章模板
     */
    private void parseAndUpdateArticle(JsonNode aiResponse, ArticleTemplate articleTemplate) {
        try {
            JsonNode titleNode = aiResponse.get("title");
            if (titleNode != null && !titleNode.asText().isEmpty()) {
                String title = titleNode.asText().trim();
                if (title != null) {
                    articleTemplate.setArticleTitle(title);
                }
            }

            // 提取摘要
            JsonNode summaryNode = aiResponse.get("summary");
            String summary = summaryNode != null ? summaryNode.asText() : "";
            
            // 提取标签
            List<ArticleTemplateTagDTO> tagList = new ArrayList<>();
            JsonNode tagsNode = aiResponse.get("tags");
            if (tagsNode != null && tagsNode.isArray()) {
                for (JsonNode tagNode : tagsNode) {
                    ArticleTemplateTagDTO tag = new ArticleTemplateTagDTO();
                    tag.setType(tagNode.get("type").asText());
                    tag.setValue(tagNode.get("value").asText());
                    tagList.add(tag);
                }
            }
            
            // 标签转换为JSON
            String tags = null;
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                tags = objectMapper.writeValueAsString(tagList);
            } catch (JsonProcessingException e) {
                logger.error("标签转换为JSON失败", e);
                tags = "[]";
            }
            
            // 提取内容长度
            JsonNode contentLengthNode = aiResponse.get("contentLength");
            String contentLength = contentLengthNode != null ? contentLengthNode.asText() : "";
            
            // 更新文章模板
            articleTemplate.setStatus(ArticleTemplateStatus.PARSED.getValue());
            articleTemplate.setSummary(summary);
            articleTemplate.setTags(tags);
            articleTemplate.setContentLength(contentLength);
            articleTemplate.setUpdateTime(new Date());
            articleTemplateMapper.update(articleTemplate);
        } catch (Exception e) {
            logger.error("处理AI响应失败", e);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "处理AI响应失败: " + e.getMessage());
        }
    }
}