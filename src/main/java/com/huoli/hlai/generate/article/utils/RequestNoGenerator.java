package com.huoli.hlai.generate.article.utils;

import java.util.UUID;

/**
 * 请求流水号生成器。
 * 用于生成唯一的请求标识号。
 *
 * @author jihao
 * @date 2026/03/13
 */
public class RequestNoGenerator {

    /**
     * 生成唯一请求流水号
     * 格式：HS + 时间戳 + UUID 前 8 位
     *
     * @return 请求流水号
     */
    public static String generate() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "HS" + timestamp + uuid;
    }
}
