package com.demo_shopyy_1.controller;

import com.demo_shopyy_1.dto.PasswordResetRequestDto;
import com.demo_shopyy_1.dto.ResetPasswordDto;
import com.demo_shopyy_1.dto.VerifyResetCodeDto;
import com.demo_shopyy_1.service.impl.PasswordResetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password-reset")
public class PasswordResetController {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetController.class);

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody PasswordResetRequestDto requestDto) {
        try {
            passwordResetService.sendResetCode(requestDto.getEmail());
            return ResponseEntity.ok("reset code sent to your email");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyResetCode(@RequestBody VerifyResetCodeDto verifyResetCodeDto) {
        log.info("Verifying reset code for email: {}", verifyResetCodeDto.getEmail());
        boolean isValid = passwordResetService.verifyResetCode(verifyResetCodeDto.getEmail(), verifyResetCodeDto.getResetCode());
        if (isValid) {
            log.warn("Invalid reset code attempt for email: {}", verifyResetCodeDto.getEmail());
            return ResponseEntity.ok("reset code verified successfully");
        } else {
            return ResponseEntity.badRequest().body("invalid reset code");
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDto resetPasswordDto) {
        try {
            passwordResetService.resetPassword(
                    resetPasswordDto.getEmail(),
                    resetPasswordDto.getNewPassword(),
                    resetPasswordDto.getConfirmPassword()
            );
            return ResponseEntity.ok("password reset successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
