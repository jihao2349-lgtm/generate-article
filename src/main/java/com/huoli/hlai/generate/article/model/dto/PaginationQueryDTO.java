package com.huoli.hlai.generate.article.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Min;

/**
 * 分页查询DTO
 * 用于文章列表查询和模板列表查询
 */
@Schema(name = "PaginationQueryDTO", description = "分页查询参数")
public class PaginationQueryDTO extends UserInfoDTO {
    /**
     * 关键词
     */
    @Schema(description = "关键词", example = "人工智能")
    private String keyword;

    /**
     * 页码
     */
    @Schema(description = "页码", defaultValue = "1", example = "1")
    @Min(value = 1, message = "页码必须大于等于1")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", defaultValue = "10", example = "10")
    @Min(value = 1, message = "每页大小必须大于等于1")
    private Integer pageSize = 10;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}