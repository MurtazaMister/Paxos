package com.lab.paxos.service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
@Slf4j
public class ClientService {

    @Autowired
    private ValidationService validationService;

    private Long userId;

    public void startClient(){
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))){
            System.out.print("username:");
            String username = reader.readLine();

            userId = validationService.identifyServer(username);

            System.out.print("password:");
            String password = reader.readLine();

            // Validating credentials
            boolean validUser = validationService.validate(userId, password);
            if(validUser){
                log.info("User validated");
            }
            else {
                throw new Exception("Invalid password");
            }

        } catch (Exception e) {
            log.trace("Exception: {}", e.getMessage());
        }
    }
}
