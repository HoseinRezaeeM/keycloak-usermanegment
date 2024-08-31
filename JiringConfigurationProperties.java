package com.uaa.client.jiring;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "com.si.client.sms.jiring")
@Setter
@Getter
@NoArgsConstructor
public class JiringConfigurationProperties {
    private String basicAuth;
    private String baseUrl;
    private String senderAddress;

}
