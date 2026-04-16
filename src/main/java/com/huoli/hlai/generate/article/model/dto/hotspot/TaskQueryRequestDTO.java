package com.huoli.hlai.generate.article.model.dto.hotspot;

import com.huoli.hlai.generate.article.model.dto.UserInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 任务结果查询请求 DTO。
 *
 * @author jihao
 * @date 2026/03/23
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "TaskQueryRequestDTO", description = "任务结果查询请求")
public class TaskQueryRequestDTO extends UserInfoDTO {

    /**
     * 任务 ID
     */
    @NotBlank(message = "任务 ID 不能为空")
    private String taskId;
}
