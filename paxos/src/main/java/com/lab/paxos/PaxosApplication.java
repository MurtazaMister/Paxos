package com.lab.paxos;

import com.lab.paxos.services.SocketService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.net.ServerSocket;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class PaxosApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaxosApplication.class, args);
	}

	@Bean
	CommandLineRunner startServer(SocketService socketService){
		return args -> {
			socketService.startServerSocket();
		};
	}

}
