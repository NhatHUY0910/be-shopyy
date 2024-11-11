package com.demo_shopyy_1.service.impl;

import com.demo_shopyy_1.entity.Cart;
import com.demo_shopyy_1.entity.User;
import com.demo_shopyy_1.dto.UserUpdateDto;
import com.demo_shopyy_1.repository.UserRepository;
import com.demo_shopyy_1.security.JwtTokenProvider;
import com.demo_shopyy_1.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FirebaseStorageService storageService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public User registerUser(String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw  new RuntimeException("email is already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        Cart cart = new Cart();
        cart.setUser(user);
        user.setCart(cart);

        return userRepository.save(user);
    }

    @Override
    public User loginUser(String email, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getPassword().equals(password)) {
            throw  new RuntimeException("Wrong password");
        }
        return user;
    }

    @Override
    public void logout(String token) {
        jwtTokenProvider.invalidateToken(token);
        SecurityContextHolder.clearContext();
        log.info("User logged out successfully");
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("Current user is not authenticated");
            throw new RuntimeException("User not logged in");
        }

        String userEmail = authentication.getName();
        log.info("Attempting to find user with email: " + userEmail);
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("User not found for email: " + userEmail);
                    return new RuntimeException("User not found");
                });
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User updateUser(UserUpdateDto userUpdateDto) throws IOException {
        User currentUser = getCurrentUser();
        log.info("Updating user profile for user: {}", currentUser.getEmail());

        if (userUpdateDto.getUsername() != null && !userUpdateDto.getUsername().isEmpty()) {
            currentUser.setUsername(userUpdateDto.getUsername());
            log.info("Updated username to: {}", userUpdateDto.getUsername());
        }

        if (userUpdateDto.getAvatarFile() != null && !userUpdateDto.getAvatarFile().isEmpty()) {
            try {
                // Delete old avatar if exists
                if (currentUser.getAvatarUrl() != null && !currentUser.getAvatarUrl().isEmpty()) {
                    log.info("Attempting to delete old avatar URL: {}", currentUser.getAvatarUrl());
                    try {
                        storageService.deleteFile(currentUser.getAvatarUrl());
                        log.info("Successfully deleted old avatar");
                    } catch (Exception e) {
                        log.warn("Failed to delete old avatar, proceeding with upload anyway. Error: {}", e.getMessage());
                        // Don't throw here, we still want to try uploading the new avatar
                    }
                }

                // Upload new avatar
                log.info("Attempting to upload new avatar file");
                String avatarUrl = storageService.uploadFile(userUpdateDto.getAvatarFile());
                currentUser.setAvatarUrl(avatarUrl);
                log.info("Successfully uploaded new avatar. New URL: {}", avatarUrl);

            } catch (IOException e) {
                log.error("Failed to handle avatar file for user: {}. Error: {}", currentUser.getEmail(), e.getMessage(), e);
                throw new IOException("Failed to upload new avatar file", e);
            }
        }

        return userRepository.save(currentUser);
    }

    @Override
    public User updateUserEntity(User user) {
        return userRepository.save(user);
    }

    @Override
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void setNewPassword(String email, String newPassword) {
        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private String extractFileNameFromUrl(String url) {
        // This implementation might need to be adjusted based on your Firebase URL structure
        return url.substring(url.lastIndexOf('/') + 1, url.indexOf('?'));
    }
}
