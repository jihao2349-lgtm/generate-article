package com.huoli.hlai.generate.article.context;

/**
 * 用户上下文
 * 用于存储和获取当前用户信息
 */
public class UserContext
{
	private static final ThreadLocal<String> USER_ID = new ThreadLocal<>();

	/**
	 * 设置用户ID
	 * @param userId 用户ID
	 */
	public static void setUserId(String userId) {
		USER_ID.set(userId);
	}

	/**
	 * 获取用户ID
	 * @return 用户ID
	 */
	public static String getUserId() {
		return USER_ID.get();
	}

	/**
	 * 清除用户ID
	 */
	public static void clear() {
		USER_ID.remove();
	}
}
