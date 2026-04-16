package com.huoli.hlai.generate.article.service.hotspot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huoli.hlai.generate.article.exception.AiAnalysisException;
import com.huoli.hlai.generate.article.exception.HotspotFetchException;
import com.huoli.hlai.generate.article.model.dto.hotspot.HotspotAnalyzeRequestDTO;
import com.huoli.hlai.generate.article.model.dto.hotspot.HotspotListRequestDTO;
import com.huoli.hlai.generate.article.model.entity.HotspotAnalysisRecordEntity;
import com.huoli.hlai.generate.article.model.vo.hotspot.HotspotAnalysisResultVO;
import com.huoli.hlai.generate.article.utils.HotspotDataConverter;
import com.huoli.hlai.generate.article.utils.RequestHashUtils;
import com.huoli.hlai.generate.article.utils.RequestNoGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 热点异步任务执行器。
 * 负责执行热点分析相关的异步任务，避免在 Service 内部自调用导致@Async 失效。
 *
 * @author jihao
 * @date 2026/03/23
 */
@Slf4j
@Component
public class HotspotAsyncExecutor {

    @Autowired
    private UniversalHotspotService universalHotspotService;

    @Autowired
    private AiModelCoordinator aiModelCoordinator;

    @Autowired
    private HotspotCacheService hotspotCacheService;

    @Autowired
    private HotspotKeywordHistoryService hotspotKeywordHistoryService;

    @Autowired
    private HotspotAnalysisRecordService hotspotAnalysisRecordService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private HotspotDataConverter hotspotDataConverter;

    /**
     * Redis Key 前缀 - 任务状态
     */
    private static final String TASK_KEY_PREFIX = "hotspot:task:";

    /**
     * 任务状态枚举
     */
    private static final String TASK_STATUS_PROCESSING = "PROCESSING";
    private static final String TASK_STATUS_SUCCESS = "SUCCESS";
    private static final String TASK_STATUS_FAILED = "FAILED";

    /**
     * 异步执行热点抓取 + AI 分析任务。
     *
     * @param taskId     任务 ID
     * @param requestDTO 请求参数
     */
    @Async("hotspotTaskExecutor")
    public void executeAnalyzeWithAiTaskAsync(String taskId, HotspotListRequestDTO requestDTO) {
        String redisKey = TASK_KEY_PREFIX + taskId;
        Long recordId = null;

        try {
            // 1. 更新状态为 PROCESSING
            updateTaskStatus(redisKey, TASK_STATUS_PROCESSING, null, null);
            log.info("[异步任务] 任务开始执行，taskId: {}, 类型：analyzeWithAi", taskId);

            // 2. 解析关键词
            String keyword = requestDTO.getKeyword();
            List<String> keywords = Arrays.asList(keyword.split(","));

            // 3. 获取 AI 模型类型
            String modelType = requestDTO.getModelType() != null ? requestDTO.getModelType() : "DEEPSEEK";

            // 4. 生成请求标识
            String requestNo = RequestNoGenerator.generate();
            String requestHash = RequestHashUtils.generateSimpleHash(keyword, modelType, requestDTO.getSources());

            // 5. 创建数据库记录
            String inputPayload = objectMapper.writeValueAsString(requestDTO);
            HotspotAnalysisRecordEntity record = hotspotDataConverter.createBaseRecord(
                    requestNo, null, "KEYWORD_AI", keyword, requestHash, inputPayload
            );
            record.setSourcePlatforms("[\"BAIDU\"]");
            recordId = hotspotAnalysisRecordService.createRecord(record);
            log.info("[异步任务] 数据库记录创建成功，taskId: {}, recordId: {}", taskId, recordId);

            // 6. 调用全网采集服务
            List<com.huoli.hlai.generate.article.model.dto.hotspot.RawHotspotItem> rawItems = universalHotspotService.fetchHotspots(keywords, requestDTO.getSources());
            log.info("[异步任务] 热点采集成功，taskId: {}, 热点数量：{}", taskId, rawItems.size());

            // 7. 调用 AI 协调器进行分析
            String aiAnalysis = aiModelCoordinator.analyze(modelType, keywords, rawItems);
            log.info("[异步任务] AI 分析完成，taskId: {}", taskId);

            // 8. 转换为 VO 对象
            HotspotAnalysisResultVO resultVO = hotspotDataConverter.convertToVO(rawItems, aiAnalysis);

            // 9. 更新数据库记录（成功）
            updateRecordSuccess(recordId, resultVO);

            // 10. 写入缓存
            String cacheKey = hotspotCacheService.generateAiCacheKey(modelType, requestHash);
            hotspotCacheService.saveToCache(cacheKey, resultVO, 60L);

            // 11. 保存关键词历史
            saveKeywordHistory(null, keyword);

            // 12. 更新 Redis 状态为 SUCCESS
            updateTaskStatus(redisKey, TASK_STATUS_SUCCESS, resultVO, null);
            log.info("[异步任务] 任务执行成功，taskId: {}, recordId: {}", taskId, recordId);

        } catch (AiAnalysisException e) {
            log.error("[异步任务] AI 分析失败，taskId: {}, recordId: {}, failureType: {}", 
                    taskId, recordId, e.getFailureType(), e);
            handleTaskFailure(taskId, redisKey, recordId, e.getMessage());
        } catch (HotspotFetchException e) {
            log.error("[异步任务] 抓取失败，taskId: {}, recordId: {}, failureType: {}", 
                    taskId, recordId, e.getFailureType(), e);
            handleTaskFailure(taskId, redisKey, recordId, e.getMessage());
        } catch (Exception e) {
            log.error("[异步任务] 执行失败，taskId: {}, recordId: {}", taskId, recordId, e);
            handleTaskFailure(taskId, redisKey, recordId, e.getMessage());
        }
    }

    /**
     * 异步执行热点分析任务。
     *
     * @param taskId     任务 ID
     * @param requestDTO 请求参数
     */
    @Async("hotspotTaskExecutor")
    public void executeAnalyzeTaskAsync(String taskId, HotspotAnalyzeRequestDTO requestDTO) {
        String redisKey = TASK_KEY_PREFIX + taskId;
        Long recordId = null;

        try {
            // 1. 更新状态为 PROCESSING
            updateTaskStatus(redisKey, TASK_STATUS_PROCESSING, null, null);
            log.info("[异步任务] 任务开始执行，taskId: {}, 类型：analyze", taskId);

            // 2. 获取 AI 模型类型
            String modelType = requestDTO.getModelType() != null ? requestDTO.getModelType() : "DEEPSEEK";

            // 3. 生成请求标识
            String requestNo = RequestNoGenerator.generate();
            String requestHash;
            try {
                requestHash = RequestHashUtils.generateSimpleHash(
                        objectMapper.writeValueAsString(requestDTO.getHotList()),
                        modelType
                );
            } catch (JsonProcessingException e) {
                log.error("[异步任务] 请求参数 JSON 序列化失败，taskId: {}", taskId, e);
                throw new RuntimeException("请求参数格式错误：" + e.getMessage(), e);
            }

            // 4. 创建数据库记录
            String inputPayload = objectMapper.writeValueAsString(requestDTO);
            HotspotAnalysisRecordEntity record = hotspotDataConverter.createBaseRecord(
                    requestNo, null, "RAW_LIST_AI", null, requestHash, inputPayload
            );
            record.setSourcePlatforms("[\"USER_PROVIDED\"]");
            recordId = hotspotAnalysisRecordService.createRecord(record);
            log.info("[异步任务] 数据库记录创建成功，taskId: {}, recordId: {}", taskId, recordId);

            // 5. 转换 DTO 为内部对象
            List<com.huoli.hlai.generate.article.model.dto.hotspot.RawHotspotItem> rawItems = hotspotDataConverter.convertToRawHotspotItems(requestDTO.getHotList());

            // 6. 调用 AI 协调器进行分析
            String aiAnalysis = aiModelCoordinator.analyze(
                    modelType,
                    hotspotDataConverter.extractKeywords(rawItems),
                    rawItems
            );
            log.info("[异步任务] AI 分析完成，taskId: {}", taskId);

            // 7. 转换为 VO 对象
            HotspotAnalysisResultVO resultVO = hotspotDataConverter.convertToVO(rawItems, aiAnalysis);

            // 8. 更新数据库记录（成功）
            updateRecordSuccess(recordId, resultVO);

            // 9. 写入缓存
            String cacheKey = hotspotCacheService.generateAiCacheKey(modelType, requestHash);
            hotspotCacheService.saveToCache(cacheKey, resultVO, 60L);

            // 10. 更新 Redis 状态为 SUCCESS
            updateTaskStatus(redisKey, TASK_STATUS_SUCCESS, resultVO, null);
            log.info("[异步任务] 任务执行成功，taskId: {}, recordId: {}", taskId, recordId);

        } catch (AiAnalysisException e) {
            log.error("[异步任务] AI 分析失败，taskId: {}, recordId: {}, failureType: {}", 
                    taskId, recordId, e.getFailureType(), e);
            handleTaskFailure(taskId, redisKey, recordId, e.getMessage());
        } catch (Exception e) {
            log.error("[异步任务] 执行失败，taskId: {}, recordId: {}", taskId, recordId, e);
            handleTaskFailure(taskId, redisKey, recordId, e.getMessage());
        }
    }

    /**
     * 处理任务失败。
     *
     * @param taskId   任务 ID
     * @param redisKey Redis Key
     * @param recordId 数据库记录 ID
     * @param errorMsg 错误信息
     */
    private void handleTaskFailure(String taskId, String redisKey, Long recordId, String errorMsg) {
        // 更新数据库记录为失败
        if (recordId != null) {
            hotspotAnalysisRecordService.markFailed(recordId, errorMsg, java.time.LocalDateTime.now());
        }

        // 更新 Redis 状态为 FAILED
        updateTaskStatus(redisKey, TASK_STATUS_FAILED, null, errorMsg);
        log.error("[异步任务] 任务执行失败，taskId: {}, recordId: {}", taskId, recordId);
    }

    /**
     * 更新任务状态到 Redis。
     *
     * @param redisKey  Redis Key
     * @param status    任务状态
     * @param result    分析结果
     * @param errorMsg  错误信息
     */
    private void updateTaskStatus(String redisKey, String status, HotspotAnalysisResultVO result, String errorMsg) {
        try {
            com.huoli.hlai.generate.article.model.vo.hotspot.HotspotTaskResultVO taskResult = com.huoli.hlai.generate.article.model.vo.hotspot.HotspotTaskResultVO.builder()
                    .taskId(extractTaskIdFromKey(redisKey))
                    .status(status)
                    .result(result)
                    .errorMsg(errorMsg)
                    .build();

            String taskJson = objectMapper.writeValueAsString(taskResult);
            stringRedisTemplate.opsForValue().set(redisKey, taskJson, 2, TimeUnit.HOURS);
            log.debug("[异步任务] 任务状态已更新，redisKey: {}, status: {}", redisKey, status);
        } catch (JsonProcessingException e) {
            log.error("[异步任务] 任务状态 JSON 序列化失败，redisKey: {}", redisKey, e);
        }
    }

    /**
     * 从 Redis Key 中提取 taskId。
     *
     * @param redisKey Redis Key
     * @return 任务 ID
     */
    private String extractTaskIdFromKey(String redisKey) {
        if (redisKey.startsWith(TASK_KEY_PREFIX)) {
            return redisKey.substring(TASK_KEY_PREFIX.length());
        }
        return redisKey;
    }

    /**
     * 更新记录为成功状态
     *
     * @param recordId 记录 ID
     * @param resultVO 热点分析结果 VO
     */
    private void updateRecordSuccess(Long recordId, HotspotAnalysisResultVO resultVO) {
        try {
            String resultJson = objectMapper.writeValueAsString(resultVO);
            hotspotAnalysisRecordService.markSuccess(
                    recordId,
                    resultJson,
                    java.time.LocalDateTime.now()
            );
            log.debug("[异步任务] 数据库记录更新成功，recordId: {}", recordId);
        } catch (Exception e) {
            log.error("[异步任务] 数据库记录更新失败，recordId: {}", recordId, e);
        }
    }

    /**
     * 保存关键词历史
     *
     * @param userId  用户 ID
     * @param keyword 关键词
     */
    private void saveKeywordHistory(Long userId, String keyword) {
        try {
            hotspotKeywordHistoryService.saveOrIncrement(
                    userId != null ? userId : -1L,
                    keyword,
                    java.time.LocalDateTime.now()
            );
            log.debug("[异步任务] 关键词历史保存成功，keyword: {}", keyword);
        } catch (Exception e) {
            log.error("[异步任务] 关键词历史保存失败，keyword: {}", keyword, e);
        }
    }
}
