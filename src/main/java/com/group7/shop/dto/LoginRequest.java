package com.group7.shop.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "用户名必填") String username,
        @NotBlank(message = "密码必填") String password
) {
}
