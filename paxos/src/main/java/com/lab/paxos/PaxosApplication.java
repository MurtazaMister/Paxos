package com.lab.paxos;

import com.lab.paxos.services.SocketService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PaxosApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaxosApplication.class, args);
	}

	@Bean
	CommandLineRunner startServer(SocketService socketService){
		CommandLineRunner commandLineRunner = new CommandLineRunner() {
			@Override
			public void run(String... args) throws Exception {
				socketService.startServerSocket();
			}
		};
		return commandLineRunner;
	}

}
