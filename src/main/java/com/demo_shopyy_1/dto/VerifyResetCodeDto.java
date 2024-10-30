package com.demo_shopyy_1.dto;

import lombok.Data;

@Data
public class VerifyResetCodeDto {
    private String email;
    private String resetCode;
}
