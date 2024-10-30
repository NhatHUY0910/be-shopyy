package com.demo_shopyy_1.exception;

public class PasswordResetLimitExceededException extends RuntimeException {
    public PasswordResetLimitExceededException(String message) {
        super(message);
    }
}
