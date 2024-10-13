package com.lab.paxos.service.client;

import com.lab.paxos.config.client.ApiConfig;
import com.lab.paxos.dto.TransactionDTO;
import com.lab.paxos.model.Transaction;
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
    private String username;
    private ApiConfig apiConfig;
    @Autowired
    private ApiService apiService;

    public void startClient(){

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))){

            System.out.print("username:");
            username = reader.readLine();

            userId = validationService.identifyServer(username);

            if(userId == -1){
                log.error("Invalid username or server error");
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
                log.error("Error while logging in, please try again in a while");
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
                        - f (fail server) <port (optional)>
                        - r (resume server) <port (optional)>
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
                            System.out.println("Balance: $"+apiService.balanceCheck(userId));
                            break;
                        case "s":
                            if(parts.length == 3 && parts[2].matches("-?\\d+")) {
                                Transaction t = apiService.transact(username, parts[1], Long.parseLong(parts[2]));
                                System.out.println(t);
                            }
                            else {
                                log.warn("Invalid command");
                            }
                            break;
                        case "e":
                            exitFlag = true;
                            break;
                        case "f":
                            if(parts.length > 1){
                                int port = Integer.parseInt(parts[1]);
                                apiService.failServer(port);
                            }
                            else{
                                apiService.failServer(null);
                            }
                            break;
                        case "r":
                            if(parts.length > 1){
                                int port = Integer.parseInt(parts[1]);
                                apiService.resumeServer(port);
                            }
                            else{
                                apiService.resumeServer(null);
                            }
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
