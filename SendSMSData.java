package com.uaa.client.jiring.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SendSMSData {

    private String SourceAddress;


    private String DestinationAddress;

    private String MessageText;
}
