package com.lab.paxos;

import com.lab.paxos.service.SocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@Slf4j
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
