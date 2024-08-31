package com.si.uaa.service.impl;


import com.uaa.api.v1.domain.request.*;
import com.uaa.api.v1.domain.response.AuthResponse;
import com.uaa.client.keycloak.KeycloakClientConfigurationProperties;
import com.uaa.client.keycloak.KeycloakTokenClient;
import com.uaa.client.keycloak.dto.KeycloakTokenResponse;
import com.uaa.domain.LogUser;
import com.uaa.domain.LoginOTPESB;
import com.uaa.exception.UserNotFoundException;
import com.uaa.repository.jpa.LogUserRepository;
import com.uaa.service.LoginOTPService;
import com.uaa.service.UserAuthService;
import io.github.resilience4j.retry.annotation.Retry;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class UserAuthServiceImpl implements UserAuthService {

    private final KeycloakTokenClient keycloakTokenClient;
    private final UsersResource usersResource;
    private final RabbitTemplate rabbitTemplate;

    private final LoginOTPService loginOTPService;
    private  final LogUserRepository logUserRepository;
    private final Keycloak keycloak;

    public UserAuthServiceImpl(KeycloakTokenClient keycloakTokenClient,
                               Keycloak keycloak,
                               KeycloakClientConfigurationProperties keycloakClientConfigurationProperties, RabbitTemplate rabbitTemplate, LoginOTPService loginOTPService, LogUserRepository logUserRepository, Keycloak keycloak1) {
        this.keycloakTokenClient = keycloakTokenClient;
        this.rabbitTemplate = rabbitTemplate;
        this.loginOTPService = loginOTPService;
        this.logUserRepository = logUserRepository;
        this.keycloak = keycloak1;

        RealmResource realmResource = keycloak.realm(keycloakClientConfigurationProperties.getRealm());
        this.usersResource = realmResource.users();
    }

    @Override
    @Retry(name = "retryKeyCloakApi" , fallbackMethod = "fallbackAfterSendOtpRetry")
    public void sendOtp(SendOTPRequest request) {
        List<UserRepresentation> userRepresentations = usersResource.search(request.getCellPhone());
        if (userRepresentations == null || userRepresentations.isEmpty()) {
            throw new UserNotFoundException();
        }

        keycloakTokenClient.sendOTPBySMS(request.getCellPhone(), request.getClientType());
    }

    @Override
    public ResponseEntity<Void> sendOtpEsb(LoginWithESBRequest request) {
        List<UserRepresentation> userRepresentations = usersResource.search(request.getCellPhone());
        if (userRepresentations == null || userRepresentations.isEmpty()) {
            throw new UserNotFoundException();
        }
        return loginOTPService.generateOTPESB(request);
    }
    @Override
    public ResponseEntity<Void> sendOtpJir(LoginWithESBRequest request) {
        List<UserRepresentation> userRepresentations = usersResource.search(request.getCellPhone());
        if (userRepresentations == null || userRepresentations.isEmpty()) {
            throw new UserNotFoundException();
        }
        return loginOTPService.generateOTPJir(request);
    }

    @Override
    public AuthResponse loginWithOTP(LoginWithOTPRequest request) {
        KeycloakTokenResponse keycloakTokenResponse = keycloakTokenClient.requestForTokenByOTP(request.getCellPhone(),
                request.getCode(), request.getClientType());
        return toAuthResponse(keycloakTokenResponse);
    }

    @Override
    public AuthResponse loginWithESB(LoginWithOTPRequest request) {
        return verifyLoginWithESB(request);
    }
    @Override
    public AuthResponse verifyLoginWithESB(LoginWithOTPRequest request) {
        LoginOTPESB loginOTPESB = loginOTPService.checkOTP(request.getCellPhone(), request.getCode());
        return loginWithoutPassword(new LoginWithoutPasswordRequest(request.getCellPhone(), request.getClientType()));
    }


    @Override
    @Retry(name = "retryKeyCloakApi" , fallbackMethod = "fallbackAfterLoginWithPasswordRetry")
    public AuthResponse loginWithPassword(LoginWithPasswordRequest request) {
        KeycloakTokenResponse keycloakTokenResponse = keycloakTokenClient.requestForTokenByPassword(request.getUsername(), request.getPassword(), request.getClientType());
        LogUser logUser =new LogUser();
        List<UserRepresentation> username = keycloak.realm("si").users().searchByUsername(request.getCellPhone(), true);
        logUser.setUserId(username.get(0).getId());
        logUser.setLastDateLogin(LocalDate.now());
        logUserRepository.save(logUser);
        return toAuthResponse(keycloakTokenResponse);
    }

    @Override
    @Retry(name = "retryKeyCloakApi" , fallbackMethod = "fallbackAfterLoginWithoutPasswordRetry")
    public AuthResponse loginWithoutPassword(LoginWithoutPasswordRequest request) {
        KeycloakTokenResponse keycloakTokenResponse = keycloakTokenClient.requestForTokenWithoutPassword(request.getUsername(), request.getClientType());
        LogUser logUser =new LogUser();
        List<UserRepresentation> username = keycloak.realm("si").users().searchByUsername(request.getUsername(), true);
        logUser.setUserId(username.get(0).getId());
        logUser.setLastDateLogin(LocalDate.now());
        logUserRepository.save(logUser);
        return toAuthResponse(keycloakTokenResponse);
    }

    @Override
    @Retry(name = "retryKeyCloakApi" , fallbackMethod = "fallbackAfterRefreshRetry")
    public AuthResponse refresh(RefreshTokenRequest request) {
        KeycloakTokenResponse keycloakTokenResponse = keycloakTokenClient.refreshToken(request.getRefreshToken(), request.getClientType());
        return toAuthResponse(keycloakTokenResponse);
    }

    private AuthResponse toAuthResponse(KeycloakTokenResponse keycloakTokenResponse) {
        AuthResponse response = new AuthResponse();
        response.setAccessToken(keycloakTokenResponse.getAccessToken());
        response.setRefreshToken(keycloakTokenResponse.getRefreshToken());
        response.setExpiresIn(keycloakTokenResponse.getExpiresIn());
        response.setRefreshExpiresIn(keycloakTokenResponse.getRefreshExpiresIn());
        return response;
    }

    public void fallbackAfterSendOtpRetry(SendOTPRequest request, Throwable ex)
            throws Throwable {
        ex.printStackTrace();
        throw ex;
    }
    public AuthResponse fallbackAfterLoginWithPasswordRetry(LoginWithPasswordRequest request, Throwable ex)
            throws Throwable {
        ex.printStackTrace();
        throw ex;
    }
    public AuthResponse fallbackAfterLoginWithoutPasswordRetry(LoginWithoutPasswordRequest request, Throwable ex)
            throws Throwable {
        ex.printStackTrace();
        throw ex;
    }
    public AuthResponse fallbackAfterRefreshRetry(RefreshTokenRequest request, Throwable ex)
            throws Throwable {
        ex.printStackTrace();
        throw ex;
    }
}
