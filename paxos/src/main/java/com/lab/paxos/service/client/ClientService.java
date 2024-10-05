package com.lab.paxos.service.client;

import com.lab.paxos.config.client.ApiConfig;
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
    private ApiConfig apiConfig;

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
                listenForCommands();
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

    private void listenForCommands(){
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))){
            String input;
            boolean logoutFlag = false, exitFlag = false;
            while(true){
                System.out.println("""
                        Enter commands:
                        - l (logout)
                        - b (check balance)
                        - s (send) <receiver_username> <amount>
                        - e (exit)
                        """);
                input = reader.readLine();

                if(input != null){
                    String[] parts = input.split(" ");

                    switch(parts[0]){
                        case "l":
                            userId = -1L;
                            logoutFlag = true;
                            break;
                        case "b":
                            break;
                        case "s":
                            break;
                        case "e":
                            exitFlag = true;
                            break;
                        default:
                            log.warn("Unknown command: {}", parts[0]);
                            break;
                    }

                    if(logoutFlag || exitFlag){
                        break;
                    }
                }
            }
            if(logoutFlag){
                log.info("Logged out successfully. Login again.");
                startClient();
            } else if (exitFlag) {
                log.info("Have a great day!");
                exitService.exitApplication(0);
            }
        }
        catch(Exception e){
            log.trace("Error reading input: {}", e.getMessage());
        }
    }
}
