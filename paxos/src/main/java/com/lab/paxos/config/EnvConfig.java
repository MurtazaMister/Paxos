package com.lab.paxos.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EnvConfig {

    private final Dotenv dotenv;

    @Autowired
    public EnvConfig(Dotenv dotenv) {
        this.dotenv = dotenv;
    }

    public void testDatabaseCredentials() {
        // Use the dbPassword
        System.out.println("Database username: " + dotenv.get("db_username"));
        System.out.println("Database password: " + dotenv.get("db_password"));
    }
}
