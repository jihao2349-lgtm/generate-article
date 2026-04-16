package com.huoli.hlai.generate.article.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huoli.hlai.generate.article.model.dto.hotspot.HotspotSourceDTO;
import com.huoli.hlai.generate.article.model.dto.hotspot.RawHotspotItem;
import com.huoli.hlai.generate.article.model.entity.HotspotAnalysisRecordEntity;
import com.huoli.hlai.generate.article.model.vo.hotspot.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 热点数据转换工具类。
 * 提供热点分析相关的数据转换和 AI 响应处理方法。
 *
 * @author jihao
 * @date 2026/03/23
 */
@Slf4j
@Component
public class HotspotDataConverter {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 创建基础记录
     *
     * @param requestNo   请求流水号
     * @param userId      用户 ID
     * @param requestType 请求类型
     * @param keyword     关键词
     * @param requestHash 请求哈希
     * @param inputPayload 输入负载
     * @return 热点分析记录实体
     */
    public HotspotAnalysisRecordEntity createBaseRecord(
            String requestNo, Long userId, String requestType,
            String keyword, String requestHash, String inputPayload) {
        return HotspotAnalysisRecordEntity.builder()
                .requestNo(requestNo)
                .userId(userId != null ? userId : -1L)
                .requestType(requestType)
                .keyword(keyword)
                .requestHash(requestHash)
                .status("INIT")
                .inputPayload(inputPayload)
                .startedTime(java.time.LocalDateTime.now())
                .build();
    }

    /**
     * 将用户提供的 DTO 转换为 RawHotspotItem
     *
     * @param hotList 热点来源 DTO 列表
     * @return 原始热点项目列表
     */
    public List<RawHotspotItem> convertToRawHotspotItems(List<HotspotSourceDTO> hotList) {
        return hotList.stream()
                .map(dto -> RawHotspotItem.builder()
                        .title(dto.getTitle())
                        .content(dto.getContent())
                        .source(dto.getSource())
                        .url("")
                        .publishTime(parsePublishTime(dto.getPublishTime()))
                        .likeCount(dto.getLikeCount() != null ? dto.getLikeCount() : 0L)
                        .commentCount(dto.getCommentCount() != null ? dto.getCommentCount() : 0L)
                        .shareCount(dto.getShareCount() != null ? dto.getShareCount() : 0L)
                        .searchIndex(null)
                        .platformType("USER_PROVIDED")
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 从热点数据中提取关键词（用于 AI 分析）
     * 简单策略：取前 3 个热点的标题前 20 个字
     *
     * @param items 原始热点项目列表
     * @return 关键词列表
     */
    public List<String> extractKeywords(List<RawHotspotItem> items) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(3, items.size()); i++) {
            String title = items.get(i).getTitle();
            if (title.length() > 20) {
                sb.append(title.substring(0, 20));
            } else {
                sb.append(title);
            }
        }
        return List.of(sb.toString());
    }

    /**
     * 将原始热点数据转换为 VO 对象（无 AI 分析）
     *
     * @param rawItems 原始热点项目列表
     * @return 热点分析结果 VO
     */
    public HotspotAnalysisResultVO convertToVO(List<RawHotspotItem> rawItems) {
        return convertToVO(rawItems, null);
    }

    /**
     * 将原始热点数据转换为 VO 对象（包含 AI 分析）
     *
     * @param rawItems   原始热点项目列表
     * @param aiAnalysis AI 分析结果
     * @return 热点分析结果 VO
     */
    public HotspotAnalysisResultVO convertToVO(List<RawHotspotItem> rawItems, String aiAnalysis) {
        List<HotspotInfoVO> hotList = rawItems.stream()
                .map(item -> HotspotInfoVO.builder()
                        .index(rawItems.indexOf(item) + 1)
                        .title(item.getTitle())
                        .content(item.getContent())
                        .source(item.getSource())
                        .heat(calculateHeat(item))
                        .build())
                .collect(Collectors.toList());

        // 如果没有 AI 分析结果，返回空的结构
        if (aiAnalysis == null || aiAnalysis.trim().isEmpty()) {
            return HotspotAnalysisResultVO.builder()
                    .hotList(hotList)
                    .coreOpinionList(List.of())
                    .controversialPerspectiveList(List.of())
                    .writingGuidanceList(HotspotWritingGuidanceVO.empty())
                    .build();
        }

        // 解析 AI 分析的 JSON 结果
        try {
            // 预处理 AI 响应，清理可能的 Markdown 格式和智能引号
            String cleanedJson = preprocessAIResponse(aiAnalysis);

            // 记录清理后的 JSON 以便调试
            log.debug("[AI 响应清理] 清理后的 JSON: {}", cleanedJson);

            JsonNode rootNode = objectMapper.readTree(cleanedJson);

            // 解析核心观点列表
            List<HotspotInsightVO> coreOpinionList = parseCoreOpinions(rootNode);

            // 解析争议视角列表
            List<HotspotInsightVO> controversialPerspectiveList = parseControversialPerspectives(rootNode);

            // 解析写作指导建议
            HotspotWritingGuidanceVO writingGuidanceList = parseWritingGuidance(rootNode);

            return HotspotAnalysisResultVO.builder()
                    .hotList(hotList)
                    .coreOpinionList(coreOpinionList)
                    .controversialPerspectiveList(controversialPerspectiveList)
                    .writingGuidanceList(writingGuidanceList)
                    .build();

        } catch (Exception e) {
            log.error("[AI 分析结果解析] 失败，返回空结构。原始 AI 响应：{}", aiAnalysis, e);
            return HotspotAnalysisResultVO.builder()
                    .hotList(hotList)
                    .coreOpinionList(List.of())
                    .controversialPerspectiveList(List.of())
                    .writingGuidanceList(HotspotWritingGuidanceVO.empty())
                    .build();
        }
    }

    /**
     * 计算热度值
     *
     * @param item 原始热点项目
     * @return 热度值
     */
    public Integer calculateHeat(RawHotspotItem item) {
        // 如果是用户提供的数据，使用固定热度值 1000
        if ("USER_PROVIDED".equals(item.getPlatformType())) {
            return 1000;
        }

        // 否则按原有逻辑计算
        long heat = 0;
        if (item.getLikeCount() != null) heat += item.getLikeCount();
        if (item.getCommentCount() != null) heat += item.getCommentCount() * 2;
        if (item.getShareCount() != null) heat += item.getShareCount() * 3;
        if (item.getSearchIndex() != null) heat += item.getSearchIndex();
        return Math.min((int) heat, 100000); // 限制最大值
    }

    /**
     * 解析发布时间
     *
     * @param publishTimeStr 发布时间字符串
     * @return 日期对象
     */
    private Date parsePublishTime(String publishTimeStr) {
        if (publishTimeStr == null || publishTimeStr.trim().isEmpty()) {
            return new Date();
        }
        try {
            // 尝试解析标准格式：yyyy-MM-dd HH:mm:ss
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                    .ofPattern("yyyy-MM-dd HH:mm:ss");
            java.time.LocalDateTime localDateTime = java.time.LocalDateTime.parse(publishTimeStr, formatter);
            return java.sql.Timestamp.valueOf(localDateTime);
        } catch (Exception e) {
            log.warn("[发布时间解析] 失败，使用当前时间，publishTimeStr: {}", publishTimeStr, e);
            return new Date();
        }
    }

    /**
     * 解析核心观点列表
     *
     * @param rootNode JSON 根节点
     * @return 核心观点列表
     * @throws Exception 解析异常
     */
    private List<HotspotInsightVO> parseCoreOpinions(JsonNode rootNode) throws Exception {
        List<HotspotInsightVO> result = new ArrayList<>();
        JsonNode coreOpinionNode = rootNode.get("coreOpinionList");

        if (coreOpinionNode != null && coreOpinionNode.isArray()) {
            for (int i = 0; i < coreOpinionNode.size(); i++) {
                JsonNode opinion = coreOpinionNode.get(i);
                result.add(HotspotInsightVO.builder()
                        .index(opinion.has("index") ? opinion.get("index").asInt() : i + 1)
                        .title(opinion.has("title") ? opinion.get("title").asText() : "")
                        .content(opinion.has("content") ? opinion.get("content").asText() : "")
                        .build());
            }
        }

        return result.isEmpty() ? List.of() : result;
    }

    /**
     * 解析争议视角列表
     *
     * @param rootNode JSON 根节点
     * @return 争议视角列表
     * @throws Exception 解析异常
     */
    private List<HotspotInsightVO> parseControversialPerspectives(JsonNode rootNode) throws Exception {
        List<HotspotInsightVO> result = new ArrayList<>();
        JsonNode perspectiveNode = rootNode.get("controversialPerspectiveList");

        if (perspectiveNode != null && perspectiveNode.isArray()) {
            for (int i = 0; i < perspectiveNode.size(); i++) {
                JsonNode perspective = perspectiveNode.get(i);
                result.add(HotspotInsightVO.builder()
                        .index(perspective.has("index") ? perspective.get("index").asInt() : i + 1)
                        .title(perspective.has("title") ? perspective.get("title").asText() : "")
                        .content(perspective.has("content") ? perspective.get("content").asText() : "")
                        .build());
            }
        }

        return result.isEmpty() ? List.of() : result;
    }

    /**
     * 解析写作指导建议
     *
     * @param rootNode JSON 根节点
     * @return 写作指导建议 VO
     * @throws Exception 解析异常
     */
    private HotspotWritingGuidanceVO parseWritingGuidance(JsonNode rootNode) throws Exception {
        JsonNode guidanceNode = rootNode.get("writingGuidanceList");

        if (guidanceNode == null) {
            return HotspotWritingGuidanceVO.empty();
        }

        List<HotspotWritingThemeVO> lightList = new ArrayList<>();
        List<HotspotWritingThemeVO> heavyList = new ArrayList<>();

        // 解析轻度写作主题
        JsonNode lightListNode = guidanceNode.get("lightList");
        if (lightListNode != null && lightListNode.isArray()) {
            for (int i = 0; i < lightListNode.size(); i++) {
                JsonNode theme = lightListNode.get(i);
                lightList.add(HotspotWritingThemeVO.builder()
                        .index(theme.has("index") ? theme.get("index").asInt() : i + 1)
                        .title(theme.has("theme") ? theme.get("theme").asText() : "")
                        .content(theme.has("angle") ? theme.get("angle").asText() : "")
                        .build());
            }
        }

        // 解析重度写作主题
        JsonNode heavyListNode = guidanceNode.get("heavyList");
        if (heavyListNode != null && heavyListNode.isArray()) {
            for (int i = 0; i < heavyListNode.size(); i++) {
                JsonNode theme = heavyListNode.get(i);
                heavyList.add(HotspotWritingThemeVO.builder()
                        .index(theme.has("index") ? theme.get("index").asInt() : i + 1)
                        .title(theme.has("theme") ? theme.get("theme").asText() : "")
                        .content(theme.has("angle") ? theme.get("angle").asText() : "")
                        .build());
            }
        }

        return HotspotWritingGuidanceVO.builder()
                .lightList(lightList.isEmpty() ? List.of() : lightList)
                .heavyList(heavyList.isEmpty() ? List.of() : heavyList)
                .build();
    }

    /**
     * 预处理 AI 响应，清理 Markdown 格式和智能引号等非标准 JSON 字符。
     *
     * @param aiResponse AI 原始响应
     * @return 清理后的 JSON 字符串
     */
    private String preprocessAIResponse(String aiResponse) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            return aiResponse;
        }

        String cleaned = aiResponse;

        // 1. 移除 Markdown 代码块标记
        cleaned = cleaned.replaceAll("```json\\s*", "");
        cleaned = cleaned.replaceAll("```\\s*", "");

        // 2. 替换所有类型的智能双引号为标准双引号
        cleaned = cleaned.replace('\u201c', '"');
        cleaned = cleaned.replace('\u201d', '"');
        cleaned = cleaned.replace('\u201f', '"');
        cleaned = cleaned.replace('\uff02', '"');
        cleaned = cleaned.replace('"', '"');
        cleaned = cleaned.replace('"', '"');

        // 3. 替换智能单引号为标准单引号
        cleaned = cleaned.replace('\u2018', '\'');
        cleaned = cleaned.replace('\u2019', '\'');
        cleaned = cleaned.replace('\u201a', '\'');
        cleaned = cleaned.replace('\u201b', '\'');
        cleaned = cleaned.replace("\u2018", "'");
        cleaned = cleaned.replace("\u2019", "'");

        // 4. 替换其他常见的智能标点符号
        cleaned = cleaned.replace("…", "...");
        cleaned = cleaned.replace("—", "-");
        cleaned = cleaned.replace("–", "-");

        // 5. 替换中文全角引号为半角引号
        cleaned = cleaned.replace('"', '"');
        cleaned = cleaned.replace('"', '"');

        // 6. 清理可能的空白字符问题
        cleaned = cleaned.trim();

        return cleaned;
    }
}
