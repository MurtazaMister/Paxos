package com.lab.paxos.config;

import com.lab.paxos.service.DatabaseResetService;
import com.lab.paxos.service.SocketService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@Slf4j
@DependsOn("dataSource")
public class DataBaseConfig {

    @Autowired
    SocketService socketService;

    @Autowired
    private DatabaseResetService databaseResetService;

    @Value("${app.developer-mode}")
    private boolean developerMode;

    @PostConstruct
    public void init() {
        if(developerMode && socketService.getAssignedPort()>0){
            log.warn("Resetting balances & transactions");
            databaseResetService.resetDatabase();
        }
    }
}
