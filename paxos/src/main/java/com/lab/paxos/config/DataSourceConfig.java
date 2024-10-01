package com.lab.paxos.config;

import com.lab.paxos.services.SocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
@Slf4j
@DependsOn("envConfig")
public class DataSourceConfig {

    @Autowired
    private SocketService socketService;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.url}")
    private String baseUrl;

    @Bean
    public DataSource dataSource() {
        log.info("In datasource");
        int assignedPort = socketService.getAssignedPort();
        String url = String.format("%s_%d", baseUrl, assignedPort);

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        log.info("Connected to database url: {}", url);

        return dataSource;
    }
}
