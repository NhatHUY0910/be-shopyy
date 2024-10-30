package com.demo_shopyy_1.service;

public interface UsersEmailService {
    void sendPasswordResetEmail(String to, String resetCode);
}
