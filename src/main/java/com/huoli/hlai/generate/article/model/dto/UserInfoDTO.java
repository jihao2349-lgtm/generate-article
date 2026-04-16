package com.huoli.hlai.generate.article.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户信息参数DTO
 */
@Setter
@Getter
@Schema(name = "UserInfoDTO", description = "用户信息参数")
public class UserInfoDTO {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "user123")
    @NotBlank(message = "userId不能为空")
    private String userId;

}
