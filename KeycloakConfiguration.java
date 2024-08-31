package com.uaa.client.keycloak;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;


@Configuration
public class KeycloakConfiguration {

    @Bean
    public Keycloak keycloak(KeycloakClientConfigurationProperties properties){
        var restEasy = ResteasyClientBuilder.newBuilder()
                .connectTimeout(50000, TimeUnit.SECONDS)
                .readTimeout(50000, TimeUnit.SECONDS)
                .build();

        return  KeycloakBuilder.builder()
                .serverUrl(properties.getHost())
                .realm("master")
                .grantType("password")
                .clientId(properties.getAdminClientId())
                .clientSecret(properties.getAdminClientSecret())
                .username(properties.getAdminUsername())
                .password(properties.getAdminPassword())
                .resteasyClient(restEasy)
                .build();
    }
}
