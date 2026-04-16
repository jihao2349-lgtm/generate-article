package com.huoli.hlai.generate.article.service.hotspot;

import com.huoli.hlai.generate.article.model.vo.hotspot.HotspotAnalysisResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 热点缓存服务。
 * 用于管理热点数据的 Redis 缓存操作。
 *
 * @author jihao
 * @date 2026/03/13
 */
@Slf4j
@Service
public class HotspotCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 缓存 Key 前缀 - 普通热点列表
     */
    private static final String CACHE_KEY_PREFIX_LIST = "hotspot:list:";

    /**
     * 缓存 Key 前缀 - AI 分析结果
     */
    private static final String CACHE_KEY_PREFIX_AI = "hotspot:ai:";

    /**
     * 默认缓存时长 - 普通列表（分钟）
     */
    private static final long DEFAULT_CACHE_TTL_LIST = 30L;

    /**
     * 默认缓存时长 - AI 分析（分钟）
     */
    private static final long DEFAULT_CACHE_TTL_AI = 60L;

    /**
     * 短缓存时长 - 空结果集（分钟）
     * 用于避免频繁请求无数据的场景
     */
    private static final long SHORT_CACHE_TTL_EMPTY = 5L;

    /**
     * 从缓存获取热点数据
     *
     * @param cacheKey 缓存 Key
     * @return 缓存的热点分析结果，不存在返回 null
     */
    public HotspotAnalysisResultVO getFromCache(String cacheKey) {
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof HotspotAnalysisResultVO) {
                log.debug("缓存命中：{}", cacheKey);
                return (HotspotAnalysisResultVO) cached;
            }
            return null;
        } catch (Exception e) {
            log.error("从缓存读取数据失败：{}", cacheKey, e);
            return null;
        }
    }

    /**
     * 将热点数据写入缓存（根据结果是否为空自动选择 TTL）
     *
     * @param cacheKey 缓存 Key
     * @param resultVO 热点分析结果
     */
    public void saveToCache(String cacheKey, HotspotAnalysisResultVO resultVO) {
        // 检查结果是否为空
        boolean isEmpty = isEmptyResult(resultVO);
        
        if (isEmpty) {
            // 空结果使用短 TTL
            saveToCache(cacheKey, resultVO, SHORT_CACHE_TTL_EMPTY);
            log.info("空结果集已缓存，TTL={}分钟", SHORT_CACHE_TTL_EMPTY);
        } else {
            // 正常结果使用默认 TTL
            saveToCache(cacheKey, resultVO, DEFAULT_CACHE_TTL_LIST);
            log.info("正常结果集已缓存，TTL={}分钟", DEFAULT_CACHE_TTL_LIST);
        }
    }

    /**
     * 判断结果是否为空
     *
     * @param resultVO 热点分析结果
     * @return true-空结果；false-非空结果
     */
    private boolean isEmptyResult(HotspotAnalysisResultVO resultVO) {
        if (resultVO == null) {
            return true;
        }
        
        // 检查热点列表是否为空
        if (resultVO.getHotList() == null || resultVO.getHotList().isEmpty()) {
            return true;
        }
        
        return false;
    }

    /**
     * 将热点数据写入缓存（指定缓存时长）
     *
     * @param cacheKey 缓存 Key
     * @param resultVO 热点分析结果
     * @param ttlMinutes 缓存时长（分钟）
     */
    public void saveToCache(String cacheKey, HotspotAnalysisResultVO resultVO, long ttlMinutes) {
        try {
            redisTemplate.opsForValue().set(cacheKey, resultVO, ttlMinutes, TimeUnit.MINUTES);
            log.debug("缓存保存成功：{}, TTL={}分钟", cacheKey, ttlMinutes);
        } catch (Exception e) {
            log.error("写入缓存失败：{}", cacheKey, e);
        }
    }

    /**
     * 生成普通热点列表缓存 Key
     *
     * @param requestHash 请求哈希
     * @return 缓存 Key
     */
    public String generateListCacheKey(String requestHash) {
        return CACHE_KEY_PREFIX_LIST + requestHash;
    }

    /**
     * 生成 AI 分析结果缓存 Key
     *
     * @param modelType AI 模型类型
     * @param requestHash 请求哈希
     * @return 缓存 Key
     */
    public String generateAiCacheKey(String modelType, String requestHash) {
        return CACHE_KEY_PREFIX_AI + modelType + ":" + requestHash;
    }

    /**
     * 删除缓存
     *
     * @param cacheKey 缓存 Key
     */
    public void deleteCache(String cacheKey) {
        try {
            Boolean deleted = redisTemplate.delete(cacheKey);
            log.debug("缓存删除{}：{}", deleted ? "成功" : "失败", cacheKey);
        } catch (Exception e) {
            log.error("删除缓存失败：{}", cacheKey, e);
        }
    }

    /**
     * 检查缓存是否存在
     *
     * @param cacheKey 缓存 Key
     * @return true-存在；false-不存在
     */
    public boolean hasCache(String cacheKey) {
        try {
            Boolean exists = redisTemplate.hasKey(cacheKey);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("检查缓存失败：{}", cacheKey, e);
            return false;
        }
    }
}
