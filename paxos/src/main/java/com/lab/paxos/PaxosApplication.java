package com.lab.paxos;

import com.lab.paxos.config.EnvConfig;
import com.lab.paxos.services.SocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class PaxosApplication {

	@Autowired
	private EnvConfig envConfig;

	public static void main(String[] args) {
		SpringApplication.run(PaxosApplication.class, args);
	}

	@Bean
	CommandLineRunner startServer(SocketService socketService){
		return args -> {
			// envConfig.testDatabaseCredentials();
			socketService.startServerSocket();
		};
	}

}
