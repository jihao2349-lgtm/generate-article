package com.huoli.hlai.generate.article.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huoli.hlai.generate.article.exception.AiAnalysisException;
import com.huoli.hlai.generate.article.exception.HotspotFetchException;
import com.huoli.hlai.generate.article.model.Result;
import com.huoli.hlai.generate.article.model.dto.hotspot.HotspotAnalyzeRequestDTO;
import com.huoli.hlai.generate.article.model.dto.hotspot.HotspotListRequestDTO;
import com.huoli.hlai.generate.article.model.dto.hotspot.HotspotSourceDTO;
import com.huoli.hlai.generate.article.model.dto.hotspot.RawHotspotItem;
import com.huoli.hlai.generate.article.model.dto.hotspot.TaskQueryRequestDTO;
import com.huoli.hlai.generate.article.model.entity.HotspotAnalysisRecordEntity;
import com.huoli.hlai.generate.article.model.enums.HotSpotSourceTypes;
import com.huoli.hlai.generate.article.model.vo.hotspot.*;
import com.huoli.hlai.generate.article.service.hotspot.*;
import com.huoli.hlai.generate.article.utils.HotspotDataConverter;
import com.huoli.hlai.generate.article.utils.RequestHashUtils;
import com.huoli.hlai.generate.article.utils.RequestNoGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 热点监控控制器。
 * 提供热点抓取和热点分析的 RESTful API 接口。
 *
 * @author jihao
 * @date 2026/03/12
 */
@Validated
@RestController
@RequestMapping("/api/hotspot")
@Tag(name = "热点监控")
@Slf4j
public class HotspotMonitorController {

    @Autowired
    private UniversalHotspotService universalHotspotService;

    @Autowired
    private HotspotCacheService hotspotCacheService;

    @Autowired
    private HotspotKeywordHistoryService hotspotKeywordHistoryService;

    @Autowired
    private HotspotAnalysisRecordService hotspotAnalysisRecordService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HotspotTaskService hotspotTaskService;

    @Autowired
    private HotspotDataConverter hotspotDataConverter;

    /**
     * 热点抓取接口。
     * 根据请求参数从各大平台抓取热点列表数据。
     *
     * @param requestDTO 热点列表请求参数
     * @return 统一的 API 响应，包含热点分析结果
     */
    @Operation(summary = "热点抓取")
    @PostMapping("/list")
    public Result<HotspotAnalysisResultVO> list(
            @Valid @RequestBody HotspotListRequestDTO requestDTO
    ) {
        Long recordId = null;
        try {
            // 1. 解析关键词
            String keyword = requestDTO.getKeyword();
            List<String> keywords = Arrays.asList(keyword.split(","));

            // 2. 生成请求标识
            String requestNo = RequestNoGenerator.generate();
            String requestHash = RequestHashUtils.generateSimpleHash(keyword, null);

            // 3. 检查缓存
            String cacheKey = hotspotCacheService.generateListCacheKey(requestHash);
            HotspotAnalysisResultVO cachedResult = hotspotCacheService.getFromCache(cacheKey);
            if (cachedResult != null) {
                log.info("[热点抓取] 缓存命中，requestNo: {}, requestHash: {}", requestNo, requestHash);
                return Result.success(cachedResult);
            }

            // 4. 创建数据库记录
            String inputPayload;
            try {
                inputPayload = objectMapper.writeValueAsString(requestDTO);
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                log.error("[热点抓取] 请求参数 JSON 序列化失败，recordId: {}", recordId, e);
                throw new RuntimeException("请求参数格式错误：" + e.getMessage(), e);
            }
            HotspotAnalysisRecordEntity record = hotspotDataConverter.createBaseRecord(
                    requestNo, null, "KEYWORD", keyword, requestHash, inputPayload
            );
            recordId = hotspotAnalysisRecordService.createRecord(record);

            // 5. 调用全网采集服务
            List<RawHotspotItem> rawItems = universalHotspotService.fetchHotspots(keywords, null);

            // 6. 转换为 VO 对象
            HotspotAnalysisResultVO resultVO = hotspotDataConverter.convertToVO(rawItems);

            // 7. 更新数据库记录（成功）
            updateRecordSuccess(recordId, resultVO);

            // 8. 写入缓存（只缓存成功结果）
            hotspotCacheService.saveToCache(cacheKey, resultVO);

            // 9. 保存关键词历史
            saveKeywordHistory(null, keyword);

            log.info("[热点抓取] 处理成功，requestNo: {}, 热点数量：{}", requestNo, rawItems.size());
            return Result.success(resultVO);

        } catch (HotspotFetchException e) {
            log.error("[热点抓取] 抓取失败，recordId: {}, failureType: {}", recordId, e.getFailureType(), e);
            // 更新数据库记录（失败）
            if (recordId != null) {
                hotspotAnalysisRecordService.markFailed(recordId, e.getMessage(), java.time.LocalDateTime.now());
            }
            // 抛出异常，让全局异常处理器处理
            throw e;
        } catch (Exception e) {
            log.error("[热点抓取] 处理失败，recordId: {}", recordId, e);
            // 更新数据库记录（失败）
            if (recordId != null) {
                hotspotAnalysisRecordService.markFailed(recordId, e.getMessage(), java.time.LocalDateTime.now());
            }
            // 抛出异常，让全局异常处理器处理
            throw e;
        }
    }

    /**
     * 热点抓取 + AI 分析接口（异步任务）。
     * 根据请求参数从各大平台抓取热点列表数据，并使用指定的 AI 模型进行分析。
     * 缓存命中时直接返回，未命中时创建异步任务并立即返回 taskId。
     *
     * @param requestDTO 热点列表请求参数
     * @return 统一的 API 响应，包含任务 ID 和提示信息
     */
    @Operation(summary = "热点抓取 + AI 分析（异步）")
    @PostMapping("/analyze-with-ai")
    public Result<TaskSubmitVO> analyzeWithAi(
            @Valid @RequestBody HotspotListRequestDTO requestDTO
    ) {
        String taskId = hotspotTaskService.submitAnalyzeWithAiTask(requestDTO);
        log.info("[AI 分析] 异步任务已提交，taskId: {}", taskId);

        TaskSubmitVO taskSubmitVO = TaskSubmitVO.builder()
                .taskId(taskId)
                .message("任务已提交，请使用 taskId 查询结果")
                .build();

        return Result.success(taskSubmitVO);
    }


    /**
     * 保存关键词历史
     */
    private void saveKeywordHistory(Long userId, String keyword) {
        try {
            hotspotKeywordHistoryService.saveOrIncrement(
                    userId != null ? userId : -1L,
                    keyword,
                    java.time.LocalDateTime.now()
            );
        } catch (Exception e) {
            log.error("[关键词历史] 保存失败，keyword: {}", keyword, e);
        }
    }

    /**
     * 热点分析接口（异步任务）。
     * 对指定的热点列表进行深度分析，生成热点分析报告。
     * 缓存命中时直接返回，未命中时创建异步任务并立即返回 taskId。
     *
     * @param requestDTO 热点分析请求参数
     * @return 统一的 API 响应，包含任务 ID 和提示信息
     */
    @Operation(summary = "热点分析（异步）")
    @PostMapping("/analyze")
    public Result<TaskSubmitVO> analyze(
            @Valid @RequestBody HotspotAnalyzeRequestDTO requestDTO
    ) {
        String taskId = hotspotTaskService.submitAnalyzeTask(requestDTO);
        log.info("[热点分析] 异步任务已提交，taskId: {}", taskId);

        TaskSubmitVO taskSubmitVO = TaskSubmitVO.builder()
                .taskId(taskId)
                .message("任务已提交，请使用 taskId 查询结果")
                .build();

        return Result.success(taskSubmitVO);
    }

    /**
     * 查询异步任务结果。
     * 根据 taskId 查询热点分析任务的执行状态和结果。
     *
     * @param requestDTO 任务查询请求参数
     * @return 统一的 API 响应，包含任务状态和结果
     */
    @Operation(summary = "查询异步任务结果")
    @PostMapping("/task/query")
    public Result<HotspotTaskResultVO> getTaskResult(@Valid @RequestBody TaskQueryRequestDTO requestDTO) {
        return Result.success(hotspotTaskService.getTaskResult(requestDTO.getTaskId()));
    }

    /**
     * 获取热点来源类型列表。
     * 返回所有支持的热点来源类型枚举列表。
     *
     * @return 统一的 API 响应，包含热点来源类型列表
     */
    @Operation(summary = "获取热点来源类型列表")
    @GetMapping("/source-types")
    public Result<List<HotspotSourceTypeVO>> getSourceTypes() {
        log.info("[热点来源类型] 获取热点来源类型列表");

        List<HotspotSourceTypeVO> sourceTypes = Arrays.stream(HotSpotSourceTypes.values())
                .map(sourceType -> HotspotSourceTypeVO.builder()
                        .code(sourceType.getCode())
                        .name(sourceType.getName())
                        .build())
                .collect(Collectors.toList());

        log.info("[热点来源类型] 获取成功，数量：{}", sourceTypes.size());
        return Result.success(sourceTypes);
    }

    /**
     * 更新记录为成功状态
     */
    private void updateRecordSuccess(Long recordId, HotspotAnalysisResultVO resultVO) {
        try {
            String resultJson = objectMapper.writeValueAsString(resultVO);
            hotspotAnalysisRecordService.markSuccess(
                    recordId,
                    resultJson,
                    java.time.LocalDateTime.now()
            );
        } catch (Exception e) {
            log.error("[数据库] 更新记录失败，recordId: {}", recordId, e);
        }
    }
}
