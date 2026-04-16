package com.huoli.hlai.generate.article.service.hotspot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huoli.hlai.generate.article.model.dto.hotspot.HotspotAnalyzeRequestDTO;
import com.huoli.hlai.generate.article.model.dto.hotspot.HotspotListRequestDTO;
import com.huoli.hlai.generate.article.model.vo.hotspot.HotspotAnalysisResultVO;
import com.huoli.hlai.generate.article.model.vo.hotspot.HotspotTaskResultVO;
import com.huoli.hlai.generate.article.utils.HotspotDataConverter;
import com.huoli.hlai.generate.article.utils.RequestHashUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 热点任务服务类。
 * 负责处理热点分析任务的异步提交和执行。
 *
 * @author jihao
 * @date 2026/03/23
 */
@Slf4j
@Service
public class HotspotTaskService {

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

    @Autowired
    private HotspotAsyncExecutor hotspotAsyncExecutor;

    /**
     * Redis Key 前缀 - 任务状态
     */
    private static final String TASK_KEY_PREFIX = "hotspot:task:";

    /**
     * 任务状态枚举
     */
    private static final String TASK_STATUS_PENDING = "PENDING";
    private static final String TASK_STATUS_SUCCESS = "SUCCESS";
    private static final String TASK_STATUS_NOT_FOUND = "NOT_FOUND";

    /**
     * 提交热点抓取 + AI 分析任务。
     * 缓存命中时直接返回 SUCCESS 状态的 taskId，未命中时创建异步任务。
     *
     * @param requestDTO 热点列表请求参数
     * @return 任务 ID
     */
    public String submitAnalyzeWithAiTask(HotspotListRequestDTO requestDTO) {
        // 1. 解析关键词和模型类型
        String keyword = requestDTO.getKeyword();
        String modelType = requestDTO.getModelType() != null ? requestDTO.getModelType() : "DEEPSEEK";

        // 2. 生成请求标识（含 sources，确保不同来源组合使用独立缓存）
        String requestHash = RequestHashUtils.generateSimpleHash(keyword, modelType, requestDTO.getSources());

        // 3. 检查缓存
        String cacheKey = hotspotCacheService.generateAiCacheKey(modelType, requestHash);
        HotspotAnalysisResultVO cachedResult = hotspotCacheService.getFromCache(cacheKey);
        if (cachedResult != null) {
            log.info("[AI 分析] 缓存命中，modelType: {}, requestHash: {}", modelType, requestHash);
            // 缓存命中：生成正常 taskId，直接把缓存结果写入 Redis（status=SUCCESS）
            String taskId = UUID.randomUUID().toString();
            String redisKey = TASK_KEY_PREFIX + taskId;
            
            try {
                HotspotTaskResultVO successStatus = HotspotTaskResultVO.builder()
                        .taskId(taskId)
                        .status(TASK_STATUS_SUCCESS)
                        .result(cachedResult)
                        .errorMsg(null)
                        .build();
                String taskJson = objectMapper.writeValueAsString(successStatus);
                stringRedisTemplate.opsForValue().set(redisKey, taskJson, 2, TimeUnit.HOURS);
                log.info("[异步任务] 缓存命中，任务已创建，taskId: {}", taskId);
                return taskId;
            } catch (JsonProcessingException e) {
                log.error("[异步任务] 任务状态 JSON 序列化失败，taskId: {}", taskId, e);
                throw new RuntimeException("任务创建失败：" + e.getMessage(), e);
            }
        }

        // 4. 缓存未命中：创建异步任务
        String taskId = UUID.randomUUID().toString();
        String redisKey = TASK_KEY_PREFIX + taskId;

        // 5. 初始化 Redis 状态
        HotspotTaskResultVO initialStatus = HotspotTaskResultVO.builder()
                .taskId(taskId)
                .status(TASK_STATUS_PENDING)
                .result(null)
                .errorMsg(null)
                .build();

        try {
            String initialJson = objectMapper.writeValueAsString(initialStatus);
            stringRedisTemplate.opsForValue().set(redisKey, initialJson, 2, TimeUnit.HOURS);
            log.info("[异步任务] 任务创建成功，taskId: {}", taskId);
        } catch (JsonProcessingException e) {
            log.error("[异步任务] 任务状态 JSON 序列化失败，taskId: {}", taskId, e);
            throw new RuntimeException("任务创建失败：" + e.getMessage(), e);
        }

        // 6. 异步执行任务
        hotspotAsyncExecutor.executeAnalyzeWithAiTaskAsync(taskId, requestDTO);

        // 7. 立即返回 taskId
        return taskId;
    }

    /**
     * 提交热点分析任务。
     * 缓存命中时直接返回 SUCCESS 状态的 taskId，未命中时创建异步任务。
     *
     * @param requestDTO 热点分析请求参数
     * @return 任务 ID
     */
    public String submitAnalyzeTask(HotspotAnalyzeRequestDTO requestDTO) {
        // 1. 获取 AI 模型类型
        String modelType = requestDTO.getModelType() != null ? requestDTO.getModelType() : "DEEPSEEK";

        // 2. 生成请求标识
        String requestHash;
        try {
            requestHash = RequestHashUtils.generateSimpleHash(
                    objectMapper.writeValueAsString(requestDTO.getHotList()),
                    modelType
            );
        } catch (JsonProcessingException e) {
            log.error("[异步任务] 请求参数 JSON 序列化失败，taskId: {}", e);
            throw new RuntimeException("请求参数格式错误：" + e.getMessage(), e);
        }

        // 3. 检查缓存
        String cacheKey = hotspotCacheService.generateAiCacheKey(modelType, requestHash);
        HotspotAnalysisResultVO cachedResult = hotspotCacheService.getFromCache(cacheKey);
        if (cachedResult != null) {
            log.info("[热点分析] 缓存命中，modelType: {}, requestHash: {}", modelType, requestHash);
            // 缓存命中：生成正常 taskId，直接把缓存结果写入 Redis（status=SUCCESS）
            String taskId = UUID.randomUUID().toString();
            String redisKey = TASK_KEY_PREFIX + taskId;
            
            try {
                HotspotTaskResultVO successStatus = HotspotTaskResultVO.builder()
                        .taskId(taskId)
                        .status(TASK_STATUS_SUCCESS)
                        .result(cachedResult)
                        .errorMsg(null)
                        .build();
                String taskJson = objectMapper.writeValueAsString(successStatus);
                stringRedisTemplate.opsForValue().set(redisKey, taskJson, 2, TimeUnit.HOURS);
                log.info("[异步任务] 缓存命中，任务已创建，taskId: {}", taskId);
                return taskId;
            } catch (JsonProcessingException e) {
                log.error("[异步任务] 任务状态 JSON 序列化失败，taskId: {}", taskId, e);
                throw new RuntimeException("任务创建失败：" + e.getMessage(), e);
            }
        }

        // 4. 缓存未命中：创建异步任务
        String taskId = UUID.randomUUID().toString();
        String redisKey = TASK_KEY_PREFIX + taskId;

        // 5. 初始化 Redis 状态
        HotspotTaskResultVO initialStatus = HotspotTaskResultVO.builder()
                .taskId(taskId)
                .status(TASK_STATUS_PENDING)
                .result(null)
                .errorMsg(null)
                .build();

        try {
            String initialJson = objectMapper.writeValueAsString(initialStatus);
            stringRedisTemplate.opsForValue().set(redisKey, initialJson, 2, TimeUnit.HOURS);
            log.info("[异步任务] 任务创建成功，taskId: {}", taskId);
        } catch (JsonProcessingException e) {
            log.error("[异步任务] 任务状态 JSON 序列化失败，taskId: {}", taskId, e);
            throw new RuntimeException("任务创建失败：" + e.getMessage(), e);
        }

        // 6. 异步执行任务
        hotspotAsyncExecutor.executeAnalyzeTaskAsync(taskId, requestDTO);

        // 7. 立即返回 taskId
        return taskId;
    }

    /**
     * 查询异步任务结果。
     *
     * @param taskId 任务 ID
     * @return 任务结果
     */
    public HotspotTaskResultVO getTaskResult(String taskId) {
        try {
            String redisKey = TASK_KEY_PREFIX + taskId;

            // 从 Redis 查询任务状态
            String taskJson = stringRedisTemplate.opsForValue().get(redisKey);

            if (taskJson == null || taskJson.trim().isEmpty()) {
                // 任务不存在或已过期
                log.warn("[异步任务] 任务不存在或已过期，taskId: {}", taskId);
                return HotspotTaskResultVO.builder()
                        .taskId(taskId)
                        .status(TASK_STATUS_NOT_FOUND)
                        .result(null)
                        .errorMsg("任务不存在或已过期")
                        .build();
            }

            // 反序列化为 HotspotTaskResultVO
            HotspotTaskResultVO taskResult = objectMapper.readValue(taskJson, HotspotTaskResultVO.class);
            log.info("[异步任务] 查询成功，taskId: {}, status: {}", taskId, taskResult.getStatus());

            return taskResult;

        } catch (Exception e) {
            log.error("[异步任务] 查询失败，taskId: {}", taskId, e);
            throw new RuntimeException("查询任务状态失败：" + e.getMessage(), e);
        }
    }
}
