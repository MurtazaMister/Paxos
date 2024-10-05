package com.lab.paxos.config;

import com.lab.paxos.service.DatabaseResetService;
import com.lab.paxos.service.SocketService;
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
        log.debug("In datasource");
        int assignedPort = socketService.getAssignedPort();

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        if(assignedPort>0){
            String url = String.format("%sroyal_bank_%d", baseUrl, assignedPort);
            dataSource.setUrl(url);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            log.info("Connected to database url: {}", url);}
        else{
            log.info("No datasource found");
            String url = String.format("%stest", baseUrl);
            dataSource.setUrl(url);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
        }
        return dataSource;
    }
}
