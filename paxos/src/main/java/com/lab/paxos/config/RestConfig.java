package com.lab.paxos.config;

import com.lab.paxos.service.SocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.ConfigurableTomcatWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@Slf4j
@DependsOn("socketService")
public class RestConfig {

    @Autowired
    private SocketService socketService;

    @Bean
    public WebServerFactoryCustomizer<ConfigurableTomcatWebServerFactory> webServerCustomizer() {
        return factory -> {
            int socketPort = socketService.getAssignedPort();
            int restPort = socketPort + 10;
            log.info("Rest port identified: {}", restPort);
            factory.setPort(restPort);
        };
    }
}
