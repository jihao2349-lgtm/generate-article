package com.huoli.hlai.generate.article.exception;

import com.huoli.hlai.generate.article.model.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

/**
 * 全局异常处理器
 * 用于统一处理控制器层抛出的各类异常
 *
 * @author jihao
 * @date 2026/03/12
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LogManager.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理请求参数验证异常（@Valid 注解）
     *
     * @param ex 方法参数验证异常
     * @param request HTTP 请求对象
     * @return 包含验证错误信息的响应实体
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = resolveFieldErrorMessage(ex.getBindingResult().getFieldErrors());
        log.warn("Request validation failed, path={}, message={}", request.getRequestURI(), message);
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCode.PARAM_ERROR, message);
    }

    /**
     * 处理请求绑定异常
     *
     * @param ex 绑定异常
     * @param request HTTP 请求对象
     * @return 包含绑定错误信息的响应实体
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBindException(
            BindException ex,
            HttpServletRequest request
    ) {
        String message = resolveFieldErrorMessage(ex.getBindingResult().getFieldErrors());
        log.warn("Request binding failed, path={}, message={}", request.getRequestURI(), message);
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCode.PARAM_ERROR, message);
    }

    /**
     * 处理方法参数类型不匹配异常
     *
     * @param ex 方法参数类型不匹配异常
     * @param request HTTP 请求对象
     * @return 包含类型错误信息的响应实体
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String message = "参数类型错误：" + ex.getName();
        log.warn("Request type mismatch, path={}, message={}", request.getRequestURI(), message);
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCode.PARAM_ERROR, message);
    }

    /**
     * 处理缺少必要请求参数异常
     *
     * @param ex 缺少请求参数异常
     * @param request HTTP 请求对象
     * @return 包含参数缺失信息的响应实体
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<Void>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        String message = "缺少必要参数：" + ex.getParameterName();
        log.warn("Missing request parameter, path={}, message={}", request.getRequestURI(), message);
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCode.PARAM_ERROR, message);
    }

    /**
     * 处理 HTTP 消息不可读异常（JSON 解析失败等）
     *
     * @param ex HTTP 消息不可读异常
     * @param request HTTP 请求对象
     * @return 包含请求体读取错误信息的响应实体
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        log.warn("Request body unreadable, path={}", request.getRequestURI(), ex);
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ErrorCode.REQUEST_NOT_READABLE,
                ErrorCode.REQUEST_NOT_READABLE.getMessage()
        );
    }

    /**
     * 处理 HTTP 请求方法不支持的异常
     *
     * @param ex HTTP 请求方法不支持异常
     * @param request HTTP 请求对象
     * @return 包含请求方法不支持信息的响应实体
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ) {
        String message = "请求方法不支持：" + ex.getMethod();
        log.warn("Request method not supported, path={}, message={}", request.getRequestURI(), message);
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, ErrorCode.METHOD_NOT_ALLOWED, message);
    }

    /**
     * 处理 HTTP 媒体类型不支持的异常
     *
     * @param ex HTTP 媒体类型不支持异常
     * @param request HTTP 请求对象
     * @return 包含媒体类型不支持信息的响应实体
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request
    ) {
        String contentType = ex.getContentType() == null ? "" : ex.getContentType().toString();
        String message = StringUtils.hasText(contentType)
                ? "请求类型不支持：" + contentType
                : ErrorCode.MEDIA_TYPE_NOT_SUPPORTED.getMessage();
        log.warn("Media type not supported, path={}, message={}", request.getRequestURI(), message);
        return buildResponse(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                ErrorCode.MEDIA_TYPE_NOT_SUPPORTED,
                message
        );
    }

    /**
     * 处理热点抓取失败异常
     *
     * @param ex 热点抓取异常
     * @param request HTTP 请求对象
     * @return 包含热点抓取错误信息的响应实体
     */
    @ExceptionHandler(HotspotFetchException.class)
    public ResponseEntity<Result<Void>> handleHotspotFetchException(
            HotspotFetchException ex,
            HttpServletRequest request
    ) {
        log.warn(
                "Hotspot fetch exception, failureType={}, path={}, message={}",
                ex.getFailureType(),
                request.getRequestURI(),
                ex.getMessage()
        );
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.SYSTEM_ERROR,
                "热点抓取失败：" + ex.getMessage()
        );
    }

    /**
     * 处理 AI 分析失败异常
     *
     * @param ex AI 分析异常
     * @param request HTTP 请求对象
     * @return 包含 AI 分析错误信息的响应实体
     */
    @ExceptionHandler(AiAnalysisException.class)
    public ResponseEntity<Result<Void>> handleAiAnalysisException(
            AiAnalysisException ex,
            HttpServletRequest request
    ) {
        log.warn(
                "AI analysis exception, failureType={}, path={}, message={}",
                ex.getFailureType(),
                request.getRequestURI(),
                ex.getMessage()
        );
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.SYSTEM_ERROR,
                "AI 分析失败：" + ex.getMessage()
        );
    }

    /**
     * 处理业务异常
     *
     * @param ex 业务异常
     * @param request HTTP 请求对象
     * @return 包含业务错误信息的响应实体
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request
    ) {
        log.warn(
                "Business exception, code={}, path={}, message={}",
                ex.getErrorCode().getCode(),
                request.getRequestURI(),
                ex.getMessage()
        );
        return buildResponse(ex.getHttpStatus(), ex.getErrorCode(), ex.getMessage());
    }

    /**
     * 处理数据访问异常
     *
     * @param ex 数据访问异常
     * @param request HTTP 请求对象
     * @return 包含数据库访问错误信息的响应实体
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Result<Void>> handleDataAccessException(
            DataAccessException ex,
            HttpServletRequest request
    ) {
        log.error("Data access exception, path={}", request.getRequestURI(), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.DATA_ACCESS_ERROR,
                ErrorCode.DATA_ACCESS_ERROR.getMessage()
        );
    }

    /**
     * 处理其他未捕获的通用异常
     *
     * @param ex 通用异常
     * @param request HTTP 请求对象
     * @return 包含系统错误信息的响应实体
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception, path={}", request.getRequestURI(), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.SYSTEM_ERROR,
                ErrorCode.SYSTEM_ERROR.getMessage()
        );
    }

    /**
     * 构建统一的错误响应实体
     *
     * @param httpStatus HTTP 状态码
     * @param errorCode 错误码
     * @param message 错误信息
     * @return 包含错误信息的响应实体
     */
    private ResponseEntity<Result<Void>> buildResponse(
            HttpStatus httpStatus,
            ErrorCode errorCode,
            String message
    ) {
        Result<Void> result = new Result<>();
        result.setCode(0);
        result.setErrorCode(errorCode.getCode());
        result.setMessage(message);
        return ResponseEntity.status(httpStatus).body(result);
    }

    /**
     * 解析字段验证错误信息
     * 提取第一个验证错误的字段信息并生成友好的错误提示
     *
     * @param fieldErrors 字段错误列表
     * @return 格式化后的错误信息字符串
     */
    private String resolveFieldErrorMessage(List<FieldError> fieldErrors) {
        if (fieldErrors == null || fieldErrors.isEmpty()) {
            return ErrorCode.PARAM_ERROR.getMessage();
        }

        FieldError fieldError = fieldErrors.get(0);
        String defaultMessage = fieldError.getDefaultMessage();
        if (StringUtils.hasText(defaultMessage)) {
            return fieldError.getField() + ": " + defaultMessage;
        }

        return fieldError.getField() + " 参数不合法";
    }
}
