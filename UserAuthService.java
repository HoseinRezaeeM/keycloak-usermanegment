package com.uaa.service;

import com.uaa.api.v1.domain.request.*;
import com.uaa.api.v1.domain.response.AuthResponse;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;

public interface UserAuthService {
    void sendOtp(SendOTPRequest request);

    ResponseEntity<Void> sendOtpEsb(LoginWithESBRequest request);

    ResponseEntity<Void> sendOtpJir(LoginWithESBRequest request);

    AuthResponse loginWithOTP(LoginWithOTPRequest request);

    AuthResponse loginWithESB(LoginWithOTPRequest request);



    AuthResponse verifyLoginWithESB(LoginWithOTPRequest request);



    AuthResponse loginWithPassword(LoginWithPasswordRequest request);

    AuthResponse loginWithoutPassword(LoginWithoutPasswordRequest request);

    AuthResponse refresh(RefreshTokenRequest request);
}
