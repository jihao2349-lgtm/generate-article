package com.huoli.hlai.generate.article.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 分页结果DTO
 * 用于封装分页响应数据
 */
@Schema(name = "PageResult", description = "分页结果")
public class PageResult<T> {
    /**
     * 总记录数
     */
    @Schema(description = "总记录数")
    private int total;

    /**
     * 页码
     */
    @Schema(description = "页码")
    private int pageNum;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小")
    private int pageSize;

    /**
     * 数据列表
     */
    @Schema(description = "数据列表")
    private List<T> list;

    /**
     * 构造方法
     * @param total 总记录数
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     * @param list 数据列表
     */
    public PageResult(int total, int pageNum, int pageSize, List<T> list) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.list = list;
    }

    // getter和setter方法
    public long getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}