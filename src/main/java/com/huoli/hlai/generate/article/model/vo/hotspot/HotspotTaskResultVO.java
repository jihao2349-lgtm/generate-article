package com.huoli.hlai.generate.article.model.vo.hotspot;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热点任务结果 VO。
 * 用于返回异步热点分析任务的结果。
 *
 * @author jihao
 * @date 2026/03/23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotspotTaskResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务 ID
     */
    private String taskId;

    /**
     * 任务状态：PENDING（待处理）、PROCESSING（处理中）、SUCCESS（成功）、FAILED（失败）、NOT_FOUND（不存在）
     */
    private String status;

    /**
     * 热点分析结果（仅在成功时返回）
     */
    private HotspotAnalysisResultVO result;

    /**
     * 错误信息（仅在失败时返回）
     */
    private String errorMsg;
}
