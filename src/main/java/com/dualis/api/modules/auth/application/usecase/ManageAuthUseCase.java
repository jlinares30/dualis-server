package com.dualis.api.modules.auth.application.usecase;

import com.dualis.api.dto.request.LoginRequest;
import com.dualis.api.dto.request.RegisterRequest;
import com.dualis.api.dto.request.UpdateProfileRequest;
import com.dualis.api.dto.response.AuthResponse;
import com.dualis.api.dto.response.UserProfileResponse;

public interface ManageAuthUseCase {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserProfileResponse getCurrentUserProfile(String email);
    UserProfileResponse updateProfile(String email, UpdateProfileRequest request);
}
