package com.group7.shop.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = "原密码必填") String oldPassword,
        @NotBlank(message = "新密码必填") String newPassword
) {
}
