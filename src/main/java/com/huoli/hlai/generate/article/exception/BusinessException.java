package com.huoli.hlai.generate.article.exception;

import java.util.Objects;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 业务异常
 *
 * @author jihao
 * @date 2026/03/12
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final HttpStatus httpStatus;

    public BusinessException(String message) {
        this(ErrorCode.BUSINESS_ERROR, HttpStatus.BAD_REQUEST, message);
    }

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, HttpStatus.BAD_REQUEST, errorCode.getMessage());
    }

    public BusinessException(ErrorCode errorCode, String message) {
        this(errorCode, HttpStatus.BAD_REQUEST, message);
    }

    public BusinessException(
            ErrorCode errorCode,
            HttpStatus httpStatus,
            String message
    ) {
        super(message);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");
        this.httpStatus = Objects.requireNonNull(httpStatus, "httpStatus must not be null");
    }

}
