package com.huoli.hlai.generate.article.exception;

import lombok.Getter;

/**
 * 异常编码
 *
 * @author jihao
 * @date 2026/03/12
 */
@Getter
public enum ErrorCode {

    SUCCESS(10000, "成功"),

    PARAM_ERROR(10400, "请求参数错误"),

    REQUEST_NOT_READABLE(10401, "请求体解析失败"),

    RESOURCE_NOT_FOUND(10404, "请求资源不存在"),

    METHOD_NOT_ALLOWED(10405, "请求方法不支持"),

    MEDIA_TYPE_NOT_SUPPORTED(10406, "请求类型不支持"),

    BUSINESS_ERROR(20001, "业务处理失败"),

    DATA_ACCESS_ERROR(10600, "数据访问异常"),

    SYSTEM_ERROR(10500, "系统繁忙，请稍后重试");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
