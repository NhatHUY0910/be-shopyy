package com.demo_shopyy_1.dto;

import lombok.Data;

@Data
public class ResetPasswordDto {
    private String email;
    private String newPassword;
    private String confirmPassword;
}
