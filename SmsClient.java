package com.uaa.client;

import com.util.constant.ClientType;
import org.springframework.http.ResponseEntity;

public interface SmsClient {
    ResponseEntity sendOTP(String phoneNumber, String message);
}
