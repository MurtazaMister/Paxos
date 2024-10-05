package com.lab.paxos.config.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Getter
@Setter
public class ApiConfig {

    private Integer apiPort = -1;

    @Value("${rest.server.url}")
    private String restServerUrl;

    private String restServerUrlWithPort;

}
