package com.lab.paxos.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EnvConfig {

    private final Dotenv dotenv;

    @Autowired
    public EnvConfig(Dotenv dotenv) {
        this.dotenv = dotenv;
    }

    public void testDatabaseCredentials() {
        // Use the dbPassword
        log.info("Database username: {}", dotenv.get("db_username"));
        log.info("Database password: {}", dotenv.get("db_password"));
    }
}
