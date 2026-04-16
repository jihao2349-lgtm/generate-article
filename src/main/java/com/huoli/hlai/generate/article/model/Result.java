package com.huoli.hlai.generate.article.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 统一响应DTO
 * 用于所有接口的响应格式
 */
@Schema(description = "统一响应结果")
public class Result<T>
{
	/**
	 * 状态码
	 */
	@Schema(description = "状态码")
	private Integer code;

	/**
	 * 错误码
	 */
	@Schema(description = "错误码")
	private Integer errorCode;

	/**
	 * 消息
	 */
	@Schema(description = "消息")
	private String message;

	/**
	 * 数据
	 */
	@Schema(description = "数据")
	private T data;

	public static <T> Result<T> success() {
		Result<T> result = new Result<>();
		result.setCode(1);
		result.setMessage("success");
		return result;
	}

	public static <T> Result<T> fail() {
		Result<T> result = new Result<>();
		result.setCode(0);
		result.setMessage("fail");
		return result;
	}

	// 带code和message的成功
	public static <T> Result<T> success(String message) {
		Result<T> result = new Result<>();
		result.setCode(1);
		result.setMessage(message);
		return result;
	}

	public static <T> Result<T> fail(String message) {
		Result<T> result = new Result<>();
		result.setCode(0);
		result.setMessage(message);
		return result;
	}

	public static <T> Result<T> success(T data) {
		Result<T> result = new Result<>();
		result.setCode(1);
		result.setMessage("success");
		result.setData(data);
		return result;
	}

	public static <T> Result<T> fail(T data) {
		Result<T> result = new Result<>();
		result.setCode(0);
		result.setMessage("fail");
		result.setData(data);
		return result;
	}

	public static <T> Result<T> success(T data, String message) {
		Result<T> result = new Result<>();
		result.setCode(1);
		result.setData(data);
		result.setMessage(message);
		return result;
	}

	public static <T> Result<T> fail(T data, String message) {
		Result<T> result = new Result<>();
		result.setCode(0);
		result.setData(data);
		result.setMessage(message);
		return result;
	}

	public static <T> Result<T> fail(Integer errorCode, String message) {
		Result<T> result = new Result<>();
		result.setCode(0);
		result.setErrorCode(errorCode);
		result.setMessage(message);
		return result;
	}

	public static <T> Result<T> fail(Integer errorCode, String message, T data) {
		Result<T> result = new Result<>();
		result.setCode(0);
		result.setErrorCode(errorCode);
		result.setMessage(message);
		result.setData(data);
		return result;
	}

	public Integer getCode()
	{
		return code;
	}

	public void setCode(Integer code)
	{
		this.code = code;
	}

	public Integer getErrorCode()
	{
		return errorCode;
	}

	public void setErrorCode(Integer errorCode)
	{
		this.errorCode = errorCode;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public T getData()
	{
		return data;
	}

	public void setData(T data)
	{
		this.data = data;
	}
}