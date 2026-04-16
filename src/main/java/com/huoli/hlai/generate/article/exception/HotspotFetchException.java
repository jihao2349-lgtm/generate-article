package com.huoli.hlai.generate.article.exception;

import lombok.Getter;

/**
 * 热点抓取失败异常。
 * 用于表示热点数据采集过程中的各种失败场景。
 *
 * @author jihao
 * @date 2026/03/16
 */
@Getter
public class HotspotFetchException extends RuntimeException {

    /**
     * 失败类型
     */
    private final FailureType failureType;

    public HotspotFetchException(String message, FailureType failureType) {
        super(message);
        this.failureType = failureType;
    }

    public HotspotFetchException(String message, Throwable cause, FailureType failureType) {
        super(message, cause);
        this.failureType = failureType;
    }

    /**
     * 失败类型枚举
     */
    public enum FailureType {
        /**
         * 网络错误（连接超时、DNS 解析失败、服务端内部错误等）
         */
        NETWORK_ERROR,

        /**
         * 鉴权失败（API Key 无效、Token 过期等）
         */
        AUTH_ERROR,

        /**
         * 权限不足（账户配额耗尽、模型访问受限、区域策略限制等）
         */
        PERMISSION_DENIED,

        /**
         * 限流（触发 API 频率限制、QPS/QPM/配额超限）
         */
        RATE_LIMITED,

        /**
         * 请求无效（客户端请求格式错误、参数缺失、字段类型错误等）
         */
        INVALID_REQUEST,

        /**
         * 响应格式异常（无法解析的响应结构、API 返回业务错误等）
         */
        INVALID_RESPONSE_FORMAT,

        /**
         * 数据解析失败（JSON 格式错误、字段缺失、转换失败等）
         */
        DATA_PARSE_ERROR
    }
}
