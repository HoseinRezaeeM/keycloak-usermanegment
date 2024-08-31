package com.uaa.client.jiring;

import com.uaa.client.SmsClient;
import com.uaa.client.jiring.request.SendSMSData;
import com.util.constant.ClientType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Component
@Qualifier("JiringClient")
public class JiringClient implements SmsClient {

    private final JiringConfigurationProperties properties;

    private final RestTemplate restTemplate;

    @Autowired
    public JiringClient(JiringConfigurationProperties properties,
                        @Qualifier("jiringRestTemplate") RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    @Override
    public ResponseEntity<HashMap> sendOTP(String cellPhone, String message) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth("VGVqYXJhdGhvdXNobWFuZDpQMSFVQz9aTmhIQ04=");
        HttpEntity<List<SendSMSData>> entity = new HttpEntity<>(
                (Collections.singletonList(new SendSMSData(properties.getSenderAddress(),
                         cellPhone.replace("+98", "98"),message))), headers);

        ResponseEntity<HashMap> response = restTemplate.postForEntity(
                String.format("%s/api/message/send",properties.getBaseUrl()),
                entity,HashMap.class
        );
        return response;

    }
}
