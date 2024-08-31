package com.uaa.api.v1;

import com.uaa.api.v1.domain.request.*;
import com.uaa.api.v1.domain.response.AuthResponse;
import com.uaa.client.esb.EsbClient;
import com.uaa.service.UserAuthService;
import com.uaa.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/v1/users/auth")
public class UserAuthController {

    private final UserAuthService userAuthService;

    @Autowired
    public UserAuthController(UserAuthService userAuthService, UserService userService, EsbClient esbClient) {
        this.userAuthService = userAuthService;
    }

    @PostMapping(path = "/password/login")
    public ResponseEntity<AuthResponse> loginWithPassword(@Validated @RequestBody LoginWithPasswordRequest request) {
        return ResponseEntity.ok(userAuthService.loginWithPassword(request));
    }

//    @PostMapping(path = "/otp/login")
//    public ResponseEntity<AuthResponse> loginWithOTP(@Validated @RequestBody LoginWithOTPRequest request) {
//        return ResponseEntity.ok(userAuthService.loginWithOTP(request));
//    }

    @PostMapping(path = "/otp/login")
    public ResponseEntity<AuthResponse> loginWithESB(@Validated @RequestBody LoginWithOTPRequest request) {
        return ResponseEntity.ok(userAuthService.loginWithESB(request));
    }

    @PostMapping(path = "/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Validated @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(userAuthService.refresh(request));
    }

    @PostMapping( "/otp/send")
    public ResponseEntity<Void> sendOTP(@RequestBody LoginWithESBRequest request) {
       userAuthService.sendOtpJir(request);
        return ResponseEntity.ok().build();
    }

//    @PostMapping(path = "/otp/send")
//    public ResponseEntity<Void> sendOTPESP(@Validated @RequestBody LoginWithESBRequest request) {
//        userAuthService.sendOtpEsb(request);
//        return ResponseEntity.ok().build();
//
//    }

    @PostMapping("/verify-login-with-otp")
    public ResponseEntity<AuthResponse> verifyLoginWithESB(@Validated @RequestBody LoginWithOTPRequest request) {
        return ResponseEntity.ok(userAuthService.verifyLoginWithESB(request));
    }

}
