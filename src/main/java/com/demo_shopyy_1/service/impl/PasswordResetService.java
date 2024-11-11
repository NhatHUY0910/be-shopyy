package com.demo_shopyy_1.service.impl;

import com.demo_shopyy_1.entity.User;
import com.demo_shopyy_1.service.UserService;
import com.demo_shopyy_1.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class PasswordResetService {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    private final Random random = new Random();

    public String generateResetCode() {
        return String.format("%04d", random.nextInt(10000));
    }

    public void sendResetCode(String email) {
        User user = userService.getUserByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        String resetCode = generateResetCode();
        user.setResetCode(resetCode);
        user.setResetCodeExpiry(LocalDateTime.now().plusMinutes(3));
        userService.updateUserEntity(user);

        emailService.sendPasswordResetEmail(user.getEmail(), resetCode);
    }

    public boolean verifyResetCode(String email, String resetCode) {
        User user = userService.getUserByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (user.isResetCodeValid() && resetCode.equals(user.getResetCode())) {
            return true;
        } else {
            user.clearResetCode();
            userService.updateUserEntity(user);
            return false;
        }
    }

    public void resetPassword(String email, String newPassword, String confirmPassword) {
        User user = userService.getUserByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (!user.isResetCodeValid()) {
            throw new RuntimeException("Reset code has expired");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Passwords do not match");
        }

        userService.setNewPassword(email, newPassword);
        user.clearResetCode();
        userService.updateUserEntity(user);
    }
}
