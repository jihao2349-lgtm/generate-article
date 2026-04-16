package com.huoli.hlai.generate.article.model.enums;


import lombok.Getter;

import java.util.Arrays;

/**
 * 热点抓取来源
 *
 * @author jihao
 * @since 2026/3/23 17:26
 */
@Getter
public enum HotSpotSourceTypes {

    XIAOHONSHU("xiaohonshu", "小红书"),

    WEIBO("weibo", "微博"),

    JINRITOUTIAO("jinritoutiao", "今日头条"),

    BAIJIAHAO("baijiahao", "百家号"),

    ZHIHU("zhihu", "知乎");

    final String code;
    final String name;

    HotSpotSourceTypes(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static HotSpotSourceTypes valuesByCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
