package com.lab.paxos.service.client;

import com.lab.paxos.service.ExitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
@Slf4j
public class ClientService {

    @Autowired
    private ExitService exitService;

    @Autowired
    private ValidationService validationService;

    private Long userId;

    public void startClient(){

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))){

            System.out.print("username:");
            String username = reader.readLine();

            userId = validationService.identifyServer(username);

            if(userId == -1){
                log.error("Invalid username");
                exitService.exitApplication(0);
                return;
            }

            System.out.print("password:");
            String password = reader.readLine();

            // Validating credentials
            boolean validUser = validationService.validate(userId, password);

            if(validUser){
                log.info("User validated");
            }
            else {
                log.error("Invalid password");
                exitService.exitApplication(0);
                return;
            }

        } catch (Exception e) {
            log.trace("Exception: {}", e.getMessage());
        }
    }
}
