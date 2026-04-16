package com.huoli.hlai.generate.article.service.hotspot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huoli.hlai.generate.article.model.dto.hotspot.RawHotspotItem;
import com.huoli.hlai.generate.article.exception.HotspotFetchException;
import com.huoli.hlai.generate.article.model.enums.HotSpotSourceTypes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 全网热点采集服务。
 * 基于百度千帆 AI Search API 实现全网热点数据采集。
 * 支持从微博、知乎、抖音、百度等多个平台采集热点信息。
 *
 * @author jihao
 * @date 2026/03/13
 */
@Slf4j
@Service
public class UniversalHotspotService {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final RestTemplate restTemplate = new RestTemplate();

    @Value("${baidu.qianfan.api-key:}")
    private String baiduApiKey;

    @Value("${baidu.qianfan.base-url:https://qianfan.baidubce.com/v2}")
    private String baiduBaseUrl;

    @Value("${baidu.qianfan.model:ernie-4.5-turbo-32k}")
    private String baiduModel;

    @Value("${hotspot.time-window.days:7}")
    private int hotspotTimeWindowDays;

    /**
     * 采集全网热点数据
     *
     * @param keywords 关键词列表
     * @param sources  抓取来源类型列表（对应 HotSpotSourceTypes.code），为空则全网抓取
     * @return 原始热点数据列表
     * @throws HotspotFetchException 当抓取失败时抛出异常
     */
    public List<RawHotspotItem> fetchHotspots(List<String> keywords, List<String> sources) {
        log.info("[全网采集] 开始采集热点，关键词：{}，来源：{}", keywords, sources);

        try {
            // Step 1: 调用百度千帆 API
            String response = callBaiduQianfanAPI(keywords, sources);

            // Step 2: 解析响应数据
            List<RawHotspotItem> items = parseResponse(response);

            log.info("[全网采集] 采集完成，共 {} 条数据", items.size());
            return items;

        } catch (HotspotFetchException e) {
            // 已包装的异常直接抛出
            throw e;
        } catch (Exception e) {
            // 未预期的异常包装为 NETWORK_ERROR
            log.error("[全网采集] 采集失败", e);
            throw new HotspotFetchException("全网热点采集失败：" + e.getMessage(), e, 
                HotspotFetchException.FailureType.NETWORK_ERROR);
        }
    }

    /**
     * 调用百度千帆 AI Search API
     */
    private String callBaiduQianfanAPI(List<String> keywords, List<String> sources) throws Exception {
        String url = baiduBaseUrl + "/ai_search/chat/completions";

        // 构造用户消息内容
        String keywordStr = String.join(", ", keywords);

        // 计算时间窗口
        java.time.LocalDate endDate = java.time.LocalDate.now();
        java.time.LocalDate startDate = endDate.minusDays(hotspotTimeWindowDays);
        String timeWindowPrompt = String.format("只返回 %s 至 %s 的最新热点事件",
                startDate.toString(), endDate.toString());

        // 解析来源类型列表，生成动态搜索范围提示词
        List<HotSpotSourceTypes> sourceTypes = CollectionUtils.isEmpty(sources) ? Collections.emptyList()
                : sources.stream()
                        .map(HotSpotSourceTypes::valuesByCode)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

        String searchScopePrompt;
        String sourceFieldHint;
        if (!sourceTypes.isEmpty()) {
            String platformNames = sourceTypes.stream()
                    .map(t -> "【" + t.getName() + "】")
                    .collect(Collectors.joining("、"));
            String sourceExamples = sourceTypes.stream()
                    .map(t -> t.getName() + "热榜")
                    .collect(Collectors.joining("、"));
            searchScopePrompt = String.format("搜索范围：重点聚焦 %s 平台，优先返回这些平台的热点内容", platformNames);
            sourceFieldHint = String.format("信息来源（优先填写指定平台来源，如：%s等）", sourceExamples);
        } else {
            searchScopePrompt = "搜索范围：全网所有平台（包括但不限于微博、知乎、抖音、百度、微信、今日头条等）";
            sourceFieldHint = "信息来源（如：微博热搜、知乎热榜、抖音热点、百度热搜等）";
        }

        String userContent = """
        请搜索以下关键词相关的实时热点事件：%s

        任务要求：
        1. 基于百度搜索提供最新的热点信息
        2. 搜索关键词：%s
        3. %s
        4. %s

        输出格式要求：
        请严格按照以下 JSON 数组格式返回结果，不要有任何多余的文字：
        [
          {
            "title": "事件标题（50 字以内）",
            "content": "事件简要描述（100 字以内）",
            "source": "%s",
            "url": "相关链接地址或空字符串",
            "publishTime": "发布时间（格式：yyyy-MM-dd HH:mm:ss）",
            "likeCount": 点赞数（数字，没有则填 0）,
            "commentCount": 评论数（数字，没有则填 0）,
            "shareCount": 转发数（数字，没有则填 0）,
            "platformType": "BAIDU"
          }
        ]

        注意事项：
        - 至少返回 5 条热点数据，最多 15 条
        - 按热度从高到低排序
        - 确保数据来源可靠
        - 如果找不到相关信息，返回空数组 []
        """.formatted(keywordStr, keywordStr, searchScopePrompt, timeWindowPrompt, sourceFieldHint);

        // 构造请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("messages", List.of(
                Map.of("content", userContent, "role", "user")
        ));
        requestBody.put("search_source", "baidu_search_v1");
        requestBody.put("resource_type_filter", List.of(
                Map.of("type", "web", "top_k", 10),
                Map.of("type", "image", "top_k", 4),
                Map.of("type", "video", "top_k", 4)
        ));
        requestBody.put("search_recency_filter", "year");
        requestBody.put("stream", false);
        requestBody.put("model", baiduModel);
        requestBody.put("enable_deep_search", false);
        requestBody.put("enable_followup_query", false);
        requestBody.put("temperature", 0.11);
        requestBody.put("top_p", 0.55);
        requestBody.put("search_mode", "auto");
        requestBody.put("enable_reasoning", true);

        // 将请求体转换为 JSON 字符串以设置 Content-Length
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Appbuilder-Authorization", "Bearer " + baiduApiKey);
        headers.setContentLength(jsonBody.getBytes().length);

        // 发送 HTTP 请求
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );

        if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null) {
            return responseEntity.getBody();
        } else if (responseEntity.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            throw new HotspotFetchException(
                "百度千帆 API 鉴权失败，状态码：" + responseEntity.getStatusCode(),
                HotspotFetchException.FailureType.AUTH_ERROR
            );
        } else if (responseEntity.getStatusCode() == HttpStatus.FORBIDDEN) {
            throw new HotspotFetchException(
                "百度千帆 API 权限不足，状态码：" + responseEntity.getStatusCode(),
                HotspotFetchException.FailureType.PERMISSION_DENIED
            );
        } else if (responseEntity.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
            throw new HotspotFetchException(
                "百度千帆 API 限流，状态码：" + responseEntity.getStatusCode(),
                HotspotFetchException.FailureType.RATE_LIMITED
            );
        } else if (responseEntity.getStatusCode() == HttpStatus.BAD_REQUEST) {
            throw new HotspotFetchException(
                "百度千帆 API请求格式错误，状态码：" + responseEntity.getStatusCode(),
                HotspotFetchException.FailureType.INVALID_REQUEST
            );
        } else if (responseEntity.getStatusCode().is5xxServerError()) {
            // 5xx 服务端错误
            throw new HotspotFetchException(
                "百度千帆 API 服务端错误，状态码：" + responseEntity.getStatusCode(),
                HotspotFetchException.FailureType.NETWORK_ERROR
            );
        } else {
            // 其他错误
            throw new HotspotFetchException(
                "百度千帆 API 调用失败，状态码：" + responseEntity.getStatusCode(),
                HotspotFetchException.FailureType.NETWORK_ERROR
            );
        }
    }

    /**
     * 解析 API 响应
     *
     * @param response API 响应内容
     * @return 热点数据列表（可能为空，但表示成功无数据）
     * @throws HotspotFetchException 当解析失败时抛出异常
     */
    private List<RawHotspotItem> parseResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            // 空响应视为成功但无数据
            return Collections.emptyList();
        }

        try {
            // 先解析外层响应结构
            JsonNode rootNode = objectMapper.readTree(response);

            // 检查是否有错误
            if (rootNode.has("error")) {
                String errorMsg = rootNode.get("error").toString();
                log.error("[全网采集] API 返回错误：{}", errorMsg);
                throw new HotspotFetchException(
                    "API 返回错误：" + errorMsg,
                    HotspotFetchException.FailureType.INVALID_RESPONSE_FORMAT
                );
            }

            // 提取 choices 数组中的 content 内容
            String content = "";
            if (rootNode.has("choices") && rootNode.get("choices").isArray()) {
                JsonNode choices = rootNode.get("choices");
                if (choices.size() > 0 && choices.get(0).has("message")) {
                    JsonNode message = choices.get(0).get("message");
                    if (message.has("content")) {
                        content = message.get("content").asText();
                    }
                }
            }

            if (content.isEmpty()) {
                log.warn("[全网采集] 未找到有效响应内容");
                // 空内容视为成功但无数据
                return Collections.emptyList();
            }

            // 清理并提取 JSON 部分
            String jsonContent = extractJsonFromResponse(content);

            // 使用 Jackson 解析 JSON
            List<Map<String, Object>> jsonList = objectMapper.readValue(
                    jsonContent,
                    new TypeReference<List<Map<String, Object>>>() {
                    }
            );

            // 转换为 RawHotspotItem 对象
            return jsonList.stream()
                    .map(this::convertToRawHotspotItem)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (HotspotFetchException e) {
            // 已包装的异常直接抛出
            throw e;
        } catch (Exception e) {
            log.error("[全网采集] JSON 解析失败：{}", response, e);
            throw new HotspotFetchException(
                "热点数据解析失败：" + e.getMessage(),
                e,
                HotspotFetchException.FailureType.DATA_PARSE_ERROR
            );
        }
    }

    /**
     * 从响应中提取 JSON 内容
     */
    private String extractJsonFromResponse(String response) {
        response = response.trim();

        if (response.startsWith("```json")) {
            response = response.substring(7);
        }
        if (response.startsWith("```")) {
            response = response.substring(3);
        }
        if (response.endsWith("```")) {
            response = response.substring(0, response.length() - 3);
        }

        return response.trim();
    }

    /**
     * 将 Map 转换为 RawHotspotItem
     */
    private RawHotspotItem convertToRawHotspotItem(Map<String, Object> data) {
        try {
            return RawHotspotItem.builder()
                    .title(getStringValue(data, "title"))
                    .content(getStringValue(data, "content"))
                    .source(getStringValue(data, "source", "百度搜索"))
                    .url(getStringValue(data, "url"))
                    .publishTime(parseDate(getStringValue(data, "publishTime")))
                    .likeCount(getLongValue(data, "likeCount"))
                    .commentCount(getLongValue(data, "commentCount"))
                    .shareCount(getLongValue(data, "shareCount"))
                    .platformType(getStringValue(data, "platformType", "BAIDU"))
                    .build();
        } catch (Exception e) {
            log.warn("转换热点数据失败：{}", data, e);
            return null;
        }
    }

    private String getStringValue(Map<String, Object> data, String key, String defaultValue) {
        Object value = data.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private String getStringValue(Map<String, Object> data, String key) {
        return getStringValue(data, key, null);
    }

    private Long getLongValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) return 0L;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return new Date();
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return format.parse(dateStr);
        } catch (Exception e) {
            return new Date();
        }
    }
}
