package com.demo_shopyy_1.service.impl;

import com.demo_shopyy_1.model.Cart;
import com.demo_shopyy_1.model.User;
import com.demo_shopyy_1.repository.UserRepository;
import com.demo_shopyy_1.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
}
