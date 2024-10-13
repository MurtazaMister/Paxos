package com.lab.paxos.config.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class ApiConfig {

    private Integer apiPort = -1;

    @Value("${server.port.pool}")
    private String portPool;

    @Value("${rest.server.url}")
    private String restServerUrl;

    private String restServerUrlWithPort;

}
