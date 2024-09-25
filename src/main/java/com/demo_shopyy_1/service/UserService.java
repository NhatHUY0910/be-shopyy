package com.demo_shopyy_1.service;

import com.demo_shopyy_1.model.User;

public interface UserService {
    User registerUser(String email, String password);
    User loginUser(String email, String password);
    User getCurrentUser();
    User getUserByEmail(String email);
}
