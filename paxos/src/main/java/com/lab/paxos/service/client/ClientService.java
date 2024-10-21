package com.lab.paxos.service.client;

import com.lab.paxos.config.client.ApiConfig;
import com.lab.paxos.model.Transaction;
import com.lab.paxos.service.ExitService;
import com.lab.paxos.util.PortUtil;
import com.lab.paxos.util.ServerStatusUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

@Service
@Slf4j
@Getter
@Setter
public class ClientService {

    @Autowired
    private ExitService exitService;

    @Autowired
    @Lazy
    private ValidationService validationService;

    private Long userId;
    private String username;
    private ApiConfig apiConfig;
    private double countOfTransactionsExecuted = 0;
    private double totalTimeInProcessingTransactions = 0;
    @Autowired
    private ApiService apiService;

    @Autowired
    private CsvFileService csvFileService;
    @Autowired
    private PortUtil portUtil;
    @Value("${rest.server.url}")
    private String restServerUrl;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${rest.server.offset}")
    private String offset;
    @Autowired
    private ServerStatusUtil serverStatusUtil;

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

    public Long getId(String username){
        List<Integer> portsArray = portUtil.portPoolGenerator();

        Long id = -1L;
        int finalPort = serverStatusUtil.getActiveServer();

        String url = restServerUrl+":"+finalPort+"/user/getId";

        try {

            id = restTemplate.getForObject(UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("username", username)
                    .toUriString(), Long.class);

        } catch (HttpServerErrorException e){
            if(e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE){
                log.error("Service unavailable : {}", e.getMessage());
            } else {
                log.error("Server error: {}", e.getMessage());
            }
        }
        catch (HttpClientErrorException e) {

            if(e.getStatusCode().value() == 404){
                return -1L;
            }
            else{
                log.error(e.getMessage());
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return id;
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
                        - x (execute test file) <path (optional)>
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
                        case "x":
                            if(parts.length > 1){
                                String filePath = "";
                                for(int i = 1;i<parts.length;i++) {
                                    filePath += parts[i] + " ";
                                }
                                csvFileService.readAndExecuteCsvFile(filePath.trim(), reader);
                            }
                            else{
                                csvFileService.readAndExecuteCsvFile(reader);
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
