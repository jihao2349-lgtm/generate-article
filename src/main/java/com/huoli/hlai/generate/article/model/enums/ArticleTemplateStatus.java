package com.huoli.hlai.generate.article.model.enums;

/**
 * 文章模板状态枚举
 */
public enum ArticleTemplateStatus
{

    /**
     * 等待解析
     */
    AWAITING_PARSING("awaitingParsing", "等待解析"),

    /**
     * 解析中
     */
    PARSING("parsing", "解析中"),

    /**
     * 解析失败
     */
    PARSING_FAILED("parsingFailed", "解析失败"),

    /**
     * 已解析
     */
    PARSED("parsed", "已解析"),

    /**
     * 已收藏
     */
    FAVORITED("favorited", "已收藏"),

    /**
     * 已删除
     */
    DELETED("deleted", "已删除");

    private final String value;
    private final String description;

    /**
     * 构造方法
     * @param value 状态值
     * @param description 状态描述
     */
    ArticleTemplateStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 获取状态值
     * @return 状态值
     */
    public String getValue() {
        return value;
    }

    /**
     * 获取状态描述
     * @return 状态描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据状态值获取枚举实例
     * @param value 状态值
     * @return 枚举实例
     */
    public static ArticleTemplateStatus getByValue(String value) {
        for (ArticleTemplateStatus status : ArticleTemplateStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}