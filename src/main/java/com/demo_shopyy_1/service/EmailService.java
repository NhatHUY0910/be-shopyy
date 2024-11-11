package com.demo_shopyy_1.service;

public interface EmailService {
    void sendPasswordResetEmail(String to, String resetCode);
}
