package com.si.uaa.client.keycloak;

import com.uaa.client.keycloak.dto.KeycloakTokenResponse;
import com.uaa.client.keycloak.dto.SendOTPBySMSRequest;
import com.uaa.exception.IncorrectOTPException;
import com.uaa.exception.InvalidRefreshToken;
import com.uaa.exception.UserNotFoundException;
import com.util.constant.ClientType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotNull;
import java.util.Collections;

@Component
public class KeycloakTokenClient {

    private final KeycloakClientConfigurationProperties configuration;
    private final RestTemplate restTemplate;

    @Autowired
    public KeycloakTokenClient(KeycloakClientConfigurationProperties configuration, @Qualifier("keycloakRestTemplate") RestTemplate restTemplate) {
        this.configuration = configuration;
        this.restTemplate = restTemplate;
    }

    public void sendOTPBySMS(@NotNull String mobileNumber, @NotNull ClientType clientType) {
        var url = String.format(configuration.getHost() + "/realms/%s/send-otp-by-sms", configuration.getRealm());
        var request = buildSendOTPBySMSRequest(mobileNumber, clientType);
        try {
            restTemplate.postForEntity(url, request, Void.class);
        } catch (HttpClientErrorException e) {
            if (HttpStatus.NOT_FOUND.equals(e.getStatusCode()))
                throw new UserNotFoundException();

            throw e;
        }
    }

    public KeycloakTokenResponse requestForTokenByOTP(@NotNull String username, @NotNull String otp, @NotNull ClientType clientType) {
        var url = String.format(configuration.getHost() + "/realms/%s/protocol/openid-connect/token", configuration.getRealm());
        MultiValueMap<String, String> body = buildRequestForTokenByOTPBody(username, otp, clientType);
        HttpHeaders headers = buildRequestForTokenHeader();
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        try {
            return restTemplate.postForEntity(url, request, KeycloakTokenResponse.class).getBody();
        } catch (HttpClientErrorException e) {
            if (HttpStatus.UNAUTHORIZED.equals(e.getStatusCode()))
                throw new IncorrectOTPException();

            throw e;
        }
    }
    public KeycloakTokenResponse requestForTokenByESB(@NotNull String username, @NotNull String otp, @NotNull ClientType clientType) {
        var url = String.format(configuration.getHost() + "/realms/%s/protocol/openid-connect/token", configuration.getRealm());
        MultiValueMap<String, String> body = buildRequestForTokenByOTPBody(username, otp, clientType);
        HttpHeaders headers = buildRequestForTokenHeader();
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        try {
            return restTemplate.postForEntity(url, request, KeycloakTokenResponse.class).getBody();
        } catch (HttpClientErrorException e) {
            if (HttpStatus.UNAUTHORIZED.equals(e.getStatusCode()))
                throw new IncorrectOTPException();

            throw e;
        }
    }

    public KeycloakTokenResponse requestForTokenByPassword(@NotNull String username, @NotNull String password, @NotNull ClientType clientType) {
        var url = String.format(configuration.getHost() + "/realms/%s/protocol/openid-connect/token", configuration.getRealm());
        MultiValueMap<String, String> body = buildRequestForTokenByPasswordBody(username, password, clientType);
        HttpHeaders headers = buildRequestForTokenHeader();
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        try {
            return restTemplate.postForEntity(url, request, KeycloakTokenResponse.class).getBody();
        } catch (HttpClientErrorException e) {
            if (HttpStatus.UNAUTHORIZED.equals(e.getStatusCode()))
                throw new IncorrectOTPException();

            throw e;
        }
    }

    public KeycloakTokenResponse requestForTokenWithoutPassword(@NotNull String username, @NotNull ClientType clientType) {
        var url = String.format(configuration.getHost() + "/realms/%s/protocol/openid-connect/token", configuration.getRealm());
        MultiValueMap<String, String> body = buildRequestForTokenWithoutPasswordBody(username, clientType);
        HttpHeaders headers = buildRequestForTokenHeader();
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        return restTemplate.postForEntity(url, request, KeycloakTokenResponse.class).getBody();
    }

    public KeycloakTokenResponse refreshToken(@NotNull String refreshToken, @NotNull ClientType clientType) {
        try {
            var url = String.format(configuration.getHost() + "/realms/%s/protocol/openid-connect/token", configuration.getRealm());
            MultiValueMap<String, String> body = buildRequestForRefreshTokenBody(refreshToken, clientType);
            HttpHeaders headers = buildRequestForTokenHeader();
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            return restTemplate.postForEntity(url, request, KeycloakTokenResponse.class).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST)
                throw new InvalidRefreshToken();
            else
                throw e;
        }
    }

    private HttpHeaders buildRequestForTokenHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    private MultiValueMap<String, String> buildRequestForTokenByOTPBody(String username, String otp, ClientType clientType) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", configuration.getClientConfig(clientType).getClientId());
        body.add("client_secret", configuration.getClientConfig(clientType).getClientSecret());
        body.add("username", username);
        body.add("otp", otp);
        return body;
    }

    private MultiValueMap<String, String> buildRequestForTokenByPasswordBody(String username, String password, ClientType clientType) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", configuration.getClientConfig(clientType).getClientId());
        body.add("client_secret", configuration.getClientConfig(clientType).getClientSecret());
        body.add("username", username);
        body.add("password", password);
        return body;
    }

    private MultiValueMap<String, String> buildRequestForTokenWithoutPasswordBody(String username, ClientType clientType) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange");
        body.add("client_id", configuration.getClientConfig(clientType).getClientId());
        body.add("client_secret", configuration.getClientConfig(clientType).getClientSecret());
        body.add("requested_subject", username);
        return body;
    }

    private MultiValueMap<String, String> buildRequestForRefreshTokenBody(String refreshToken, ClientType clientType) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("client_id", configuration.getClientConfig(clientType).getClientId());
        body.add("client_secret", configuration.getClientConfig(clientType).getClientSecret());
        body.add("refresh_token", refreshToken);
        return body;
    }

    private SendOTPBySMSRequest buildSendOTPBySMSRequest(String mobileNumber, @NotNull ClientType clientType) {
        SendOTPBySMSRequest request = new SendOTPBySMSRequest();
        request.setMobileNumber(mobileNumber);
        request.setTtl(configuration.getClientConfig(clientType).getOtpConfig().getOtpTTL());
        request.setForTest(configuration.getClientConfig(clientType).getOtpConfig().isForTest());
        request.setOtpLength(configuration.getClientConfig(clientType).getOtpConfig().getOtpLength());
        request.setMessageFormat(configuration.getClientConfig(clientType).getOtpConfig().getMessageFormat());
        return request;
    }
}
