package com.shopflow.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户登录请求DTO
 */
@Data
@Schema(description = "用户登录请求")
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "john_doe", required = true)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "password123", required = true)
    private String password;
}