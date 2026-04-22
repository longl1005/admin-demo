package com.example.admin.service;

import com.example.admin.dto.LoginRequest;
import com.example.admin.dto.LoginResponse;
import com.example.admin.dto.UserInfoVO;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    UserInfoVO getCurrentUserInfo();
}
