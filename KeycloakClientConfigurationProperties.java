package com.uaa.client.keycloak;

import com.util.constant.ClientType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.List;

@Component
@ConfigurationProperties("com.si.uaa.client.keycloak")
@Setter
@Getter
@NoArgsConstructor
public class KeycloakClientConfigurationProperties {
    @NotNull
    private String host;
    @NotNull
    private String realm;
    private String adminUsername;
    private String adminPassword;
    private String adminClientId;
    private String adminClientSecret;

    @NotNull
    private List<Client> clients;

    @Setter
    @Getter
    @NoArgsConstructor
    public static class OTP {
        private boolean isForTest = true;
        private int otpLength = 6;
        private int otpTTL = 2;
        private String messageFormat = "%s";
    }

    @Setter
    @Getter
    @NoArgsConstructor
    public static class Client {
        @NotNull
        private String clientId;
        @NotNull
        private String clientSecret;
        private OTP otpConfig = new OTP();
        @NotNull
        private ClientType clientType;
    }

    public Client getClientConfig(ClientType clientType) {
        return clients.stream().filter(ct -> ct.getClientType().equals(clientType)).findFirst().orElseThrow();
    }
}
