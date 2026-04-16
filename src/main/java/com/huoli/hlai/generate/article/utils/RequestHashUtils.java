package com.huoli.hlai.generate.article.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 请求哈希工具类。
 * 用于生成请求的唯一标识哈希值，支持幂等性判断和缓存 Key 生成。
 *
 * @author jihao
 * @date 2026/03/13
 */
public class RequestHashUtils {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    /**
     * 生成请求哈希值
     *
     * @param keyword   关键词
     * @param modelType 模型类型（可选，AI 分析时需要）
     * @param userId    用户 ID
     * @return SHA-256 哈希值（十六进制字符串）
     */
    public static String generateRequestHash(String keyword, String modelType, Long userId) {
        // 将分钟时间向下取整，实现同一分钟内的相同请求哈希一致
        String timeSlot = LocalDateTime.now().format(TIME_FORMATTER);
        
        StringBuilder input = new StringBuilder()
                .append(keyword != null ? keyword : "")
                .append("|")
                .append(modelType != null ? modelType : "")
                .append("|")
                .append(userId != null ? userId : "")
                .append("|")
                .append(timeSlot);
        
        return sha256(input.toString());
    }

    /**
     * 生成简单哈希值（不包含时间，用于缓存 Key）
     *
     * @param keyword   关键词
     * @param modelType 模型类型（可选）
     * @return SHA-256 哈希值
     */
    public static String generateSimpleHash(String keyword, String modelType) {
        String input = (keyword != null ? keyword : "") + "|" + (modelType != null ? modelType : "");
        return sha256(input);
    }

    /**
     * 生成简单哈希值（不包含时间，用于缓存 Key），含来源类型列表
     *
     * @param keyword   关键词
     * @param modelType 模型类型（可选）
     * @param sources   来源类型列表（可选）
     * @return SHA-256 哈希值
     */
    public static String generateSimpleHash(String keyword, String modelType, List<String> sources) {
        String sourcesStr = (sources != null && !sources.isEmpty())
                ? String.join(",", sources.stream().sorted().toList())
                : "";
        String input = (keyword != null ? keyword : "") + "|"
                + (modelType != null ? modelType : "") + "|"
                + sourcesStr;
        return sha256(input);
    }

    /**
     * SHA-256 哈希算法
     *
     * @param input 输入字符串
     * @return 十六进制哈希字符串
     */
    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 计算失败", e);
        }
    }
}
