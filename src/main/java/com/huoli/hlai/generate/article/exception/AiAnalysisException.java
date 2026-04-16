package com.huoli.hlai.generate.article.exception;

import lombok.Getter;

/**
 * AI 分析失败异常。
 * 用于表示 AI 模型调用或结果解析过程中的失败场景。
 *
 * @author jihao
 * @date 2026/03/16
 */
@Getter
public class AiAnalysisException extends RuntimeException {

    /**
     * 失败类型
     */
    private final FailureType failureType;

    public AiAnalysisException(String message, FailureType failureType) {
        super(message);
        this.failureType = failureType;
    }

    public AiAnalysisException(String message, Throwable cause, FailureType failureType) {
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
         * 限流（触发 API 频率限制、RPM/TPM 超限）
         */
        RATE_LIMITED,

        /**
         * 请求无效（客户端请求格式错误、参数缺失、字段类型错误等）
         */
        INVALID_REQUEST,

        /**
         * 模型不存在（模型名称错误、使用了已下线的模型等）
         */
        MODEL_NOT_FOUND,

        /**
         * AI 响应格式异常（非预期的响应结构、choices 为空等）
         */
        INVALID_RESPONSE_FORMAT,

        /**
         * JSON 解析失败（AI 返回的内容无法解析为预期 JSON）
         */
        JSON_PARSE_ERROR,

        /**
         * 结果验证失败（AI 返回的内容不符合业务规则）
         */
        VALIDATION_ERROR
    }
}
