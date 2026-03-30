package com.shopflow.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 密码更新请求DTO
 */
@Data
@Schema(description = "密码更新请求")
public class PasswordUpdateDTO {

    @NotBlank(message = "原密码不能为空")
    @Schema(description = "原密码", example = "oldpassword123", required = true)
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度必须在6-20个字符之间")
    @Schema(description = "新密码", example = "newpassword123", required = true)
    private String newPassword;
}