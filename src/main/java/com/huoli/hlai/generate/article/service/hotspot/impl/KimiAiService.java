package com.huoli.hlai.generate.article.service.hotspot.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huoli.hlai.generate.article.model.dto.hotspot.RawHotspotItem;
import com.huoli.hlai.generate.article.exception.AiAnalysisException;
import com.huoli.hlai.generate.article.service.hotspot.AiModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kimi AI 模型服务实现。
 * 基于 Moonshot AI (Kimi) API 提供热点数据分析能力。
 *
 * @author jihao
 * @date 2026/03/13
 */
@Slf4j
@Service
public class KimiAiService implements AiModelService {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final RestTemplate restTemplate = new RestTemplate();

    private final ChatClient chatClient;

    @Value("${kimi.api-key:}")
    private String kimiApiKey;

    @Value("${kimi.base-url:https://api.moonshot.cn/v1}")
    private String kimiBaseUrl;

    @Value("${kimi.model:moonshot-v1-8k}")
    private String kimiModel;

    public KimiAiService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public boolean supports(String modelType) {
        return "KIMI".equalsIgnoreCase(modelType);
    }

    @Override
    public String getModelType() {
        return "KIMI";
    }

    @Override
    public String analyze(List<String> keywords, List<RawHotspotItem> hotspots) {
        log.info("[Kimi] 开始分析热点数据，关键词：{}，热点数量：{}", keywords, hotspots.size());

        try {
            // Step 1: 构造分析 Prompt
            String prompt = buildAnalysisPrompt(keywords, hotspots);

            // Step 2: 调用 Kimi API
            String response = callKimiApi(prompt);

            // Step 3: 解析并返回结果
            log.info("[Kimi] 分析完成");
            return response;

        } catch (AiAnalysisException e) {
            // 已包装的异常直接抛出
            throw e;
        } catch (Exception e) {
            // 未预期的异常包装为 NETWORK_ERROR
            log.error("[Kimi] 分析失败", e);
            throw new AiAnalysisException(
                "AI 分析失败：" + e.getMessage(), 
                e, 
                AiAnalysisException.FailureType.NETWORK_ERROR
            );
        }
    }

    /**
     * 构造热点分析 Prompt（与 DeepSeek 类似，可根据 Kimi 特点调整）
     */
    private String buildAnalysisPrompt(List<String> keywords, List<RawHotspotItem> hotspots) {
        StringBuilder hotspotText = new StringBuilder();
        for (int i = 0; i < Math.min(hotspots.size(), 10); i++) {
            RawHotspotItem item = hotspots.get(i);
            hotspotText.append((i + 1)).append(". ").append(item.getTitle())
                    .append(" - ").append(item.getContent())
                    .append(" [来源：").append(item.getSource()).append("]\n");
        }

        return """
        你是一位专业的热点分析师。请根据以下热点数据进行深度分析：
        
        搜索关键词：%s
        
        热点列表：
        %s
        
        请严格按照以下 JSON 格式返回分析结果（不要有任何多余的文字）：
        {
          "coreOpinionList": [
            {
              "index": 1,
              "title": "核心观点标题",
              "content": "核心观点详细描述"
            }
          ],
          "controversialPerspectiveList": [
            {
              "index": 1,
              "title": "争议视角标题",
              "content": "争议视角详细描述"
            }
          ],
          "writingGuidanceList": {
            "lightList": [
              {
                "theme": "轻度写作主题",
                "angle": "切入角度"
              }
            ],
            "heavyList": [
              {
                "theme": "重度写作主题",
                "angle": "切入角度"
              }
            ]
          }
        }
        
        要求：
        - coreOpinionList：至少 3 个核心观点
        - controversialPerspectiveList：至少 2 个争议视角
        - writingGuidanceList.lightList：至少 3 个轻度写作主题
        - writingGuidanceList.heavyList：至少 2 个重度写作主题
        - 所有字段必须填写，不能为空
        """.formatted(
                String.join(", ", keywords),
                hotspotText.toString()
        );
    }

    /**
     * 调用 Kimi API
     *
     * @param prompt 分析提示词
     * @return AI 响应内容
     * @throws AiAnalysisException 当调用或解析失败时抛出异常
     */
    private String callKimiApi(String prompt) throws Exception {
        String url = kimiBaseUrl + "/chat/completions";

        // 构造请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", kimiModel);
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2000);

        // 将请求体转换为 JSON 字符串
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + kimiApiKey);
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
            // 解析响应
            JsonNode rootNode = objectMapper.readTree(responseEntity.getBody());
            if (rootNode.has("choices") && rootNode.get("choices").isArray()) {
                JsonNode choices = rootNode.get("choices");
                if (choices.size() > 0 && choices.get(0).has("message")) {
                    JsonNode message = choices.get(0).get("message");
                    if (message.has("content")) {
                        return message.get("content").asText();
                    }
                }
            }
            throw new AiAnalysisException(
                "Kimi API 响应格式异常",
                AiAnalysisException.FailureType.INVALID_RESPONSE_FORMAT
            );
        } else if (responseEntity.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            throw new AiAnalysisException(
                "Kimi API 鉴权失败，状态码：" + responseEntity.getStatusCode(),
                AiAnalysisException.FailureType.AUTH_ERROR
            );
        } else if (responseEntity.getStatusCode() == HttpStatus.FORBIDDEN) {
            throw new AiAnalysisException(
                "Kimi API 权限不足，状态码：" + responseEntity.getStatusCode(),
                AiAnalysisException.FailureType.PERMISSION_DENIED
            );
        } else if (responseEntity.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new AiAnalysisException(
                "Kimi API 模型不存在，状态码：" + responseEntity.getStatusCode(),
                AiAnalysisException.FailureType.MODEL_NOT_FOUND
            );
        } else if (responseEntity.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
            throw new AiAnalysisException(
                "Kimi API 限流，状态码：" + responseEntity.getStatusCode(),
                AiAnalysisException.FailureType.RATE_LIMITED
            );
        } else if (responseEntity.getStatusCode() == HttpStatus.BAD_REQUEST) {
            throw new AiAnalysisException(
                "Kimi API请求格式错误，状态码：" + responseEntity.getStatusCode(),
                AiAnalysisException.FailureType.INVALID_REQUEST
            );
        } else if (responseEntity.getStatusCode().is5xxServerError()) {
            // 5xx 服务端错误
            throw new AiAnalysisException(
                "Kimi API 服务端错误，状态码：" + responseEntity.getStatusCode(),
                AiAnalysisException.FailureType.NETWORK_ERROR
            );
        } else {
            // 其他错误
            throw new AiAnalysisException(
                "Kimi API 调用失败，状态码：" + responseEntity.getStatusCode(),
                AiAnalysisException.FailureType.NETWORK_ERROR
            );
        }
    }
}
