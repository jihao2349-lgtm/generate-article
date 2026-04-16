package com.huoli.hlai.generate.article.model.vo.hotspot;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务提交响应 VO。
 * 用于返回异步任务的提交结果，包含任务 ID 和提示信息。
 *
 * @author jihao
 * @date 2026/03/23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSubmitVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务 ID
     */
    private String taskId;

    /**
     * 提示信息
     */
    private String message;
}
