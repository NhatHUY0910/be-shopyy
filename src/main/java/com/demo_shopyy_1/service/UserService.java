package com.demo_shopyy_1.service;

import com.demo_shopyy_1.entity.User;
import com.demo_shopyy_1.dto.UserUpdateDto;

import java.io.IOException;

public interface UserService {
    User registerUser(String email, String password);
    User loginUser(String email, String password);
    User getCurrentUser();
    User getUserByEmail(String email);
    User updateUser(UserUpdateDto userUpdateDto) throws IOException;
}
