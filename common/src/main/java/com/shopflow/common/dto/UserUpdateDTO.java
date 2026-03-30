package com.shopflow.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

/**
 * 用户信息更新请求DTO
 */
@Data
@Schema(description = "用户信息更新请求")
public class UserUpdateDTO {

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "newemail@example.com")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13900139000")
    private String phone;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;
}