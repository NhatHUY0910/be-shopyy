package com.demo_shopyy_1.controller;

import com.demo_shopyy_1.entity.User;
import com.demo_shopyy_1.dto.LoginDto;
import com.demo_shopyy_1.dto.UserUpdateDto;
import com.demo_shopyy_1.security.JwtTokenProvider;
import com.demo_shopyy_1.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
//@CrossOrigin(value = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User registerUser = userService.registerUser(user.getEmail(), user.getPassword());
            Map<String, Object> response = new HashMap<>();
            response.put("user", registerUser);
            response.put("cartId", registerUser.getCart().getId());
            return ResponseEntity.ok("User registered successfully " + response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
        public ResponseEntity<?> loginUser(@RequestBody LoginDto loginDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtTokenProvider.generateToken(authentication);
            User loggedInUser = userService.getUserByEmail(loginDto.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("user", loggedInUser);
            response.put("cartId", loggedInUser.getCart().getId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid email or password: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("User logged out successfully");
    }

//    @GetMapping("/check-login")
//    public ResponseEntity<?> checkLoginStatus() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
//            User user = userService.getCurrentUser();
//            return ResponseEntity.ok(Map.of("loggedIn", true, "user", user));
//        } else {
//            return ResponseEntity.ok(Map.of("loggedIn", false));
//        }
//    }

    @GetMapping("/check-auth")
    public ResponseEntity<?> checkAuthStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            User user = userService.getCurrentUser();
            return ResponseEntity.ok(Map.of("authenticated", true, "user", user));
        } else {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        try {
            User user = userService.getCurrentUser();
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@ModelAttribute UserUpdateDto userUpdateDto) {
        try {
            User updatedUser = userService.updateUser(userUpdateDto);
            return ResponseEntity.ok(updatedUser);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Failed to update user profile: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
