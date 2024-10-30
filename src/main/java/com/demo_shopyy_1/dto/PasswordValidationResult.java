package com.demo_shopyy_1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordValidationResult {
    private boolean isValid;
    private List<String> errors;
}
