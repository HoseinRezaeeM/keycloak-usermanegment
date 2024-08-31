package com.uaa.client.keycloak.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class SendOTPBySMSRequest {
    @NotEmpty
    private String mobileNumber;
    private Integer ttl = 2; //In Minutes
    private Integer otpLength;
    @JsonProperty("isForTest")
    private boolean isForTest;
    private String messageFormat="%s";
}
