package com.lab.paxos.service.client;

import com.lab.paxos.model.Transaction;
import com.lab.paxos.service.ExitService;
import com.lab.paxos.util.ParseUtil;
import com.lab.paxos.util.PortUtil;
import com.lab.paxos.util.ServerStatusUtil;
import com.lab.paxos.util.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class CsvFileService {

    @Value("${test.csv.file.path}")
    String filePath;
    @Value("${rest.server.url}")
    private String restServerUrl;
    @Value("${rest.server.offset}")
    private int offset;

    @Autowired
    ApiService apiService;

    @Autowired
    ParseUtil parseUtil;
    @Autowired
    @Lazy
    private ServerStatusUtil serverStatusUtil;
    @Autowired
    @Lazy
    private PortUtil portUtil;
    @Autowired
    @Lazy
    private ExitService exitService;
    @Autowired
    @Lazy
    private ClientService clientService;

    public void readAndExecuteCsvFile() {
        readAndExecuteCsvFile(filePath);
    }

    public void readAndExecuteCsvFile(String filePath) {
        Path path = Paths.get(filePath);
        char baseUname = 'a';
        String url;
        List<Integer> ports = portUtil.portPoolGenerator();

        try (BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
            Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT)){

            int currentSetNumber = 0;
            List<Integer> activeServerIds;
            Transaction currentTransaction;

            log.info("Beginning file read, enter continue");
            char sender_name , receiver_name;

            for(CSVRecord record : csvParser) {
                Integer setNumber = tryParseSetNumber(record.get(0));
                if(setNumber != null) {
                    boolean exit = promptUser(inputReader);

                    if(exit){
                        log.warn("If you exit, you will lose data and might have to re-run the file again");
                        log.warn("Transactions upto this point has been committed, if you exit, those transactions will run again and data will become inconsistent");
                        log.warn("To reset the database, run the servers once again with \"app.developer-mode=true\" in application.properties");
                        System.out.println("Exit? (Y/n)");
                        String ans = inputReader.readLine();
                        if(ans.equals("Y")){
                            exitService.exitApplication(0);
                        }
                    }

                    currentSetNumber = setNumber;
                    log.warn("Beginning execution of set number: {}", currentSetNumber);
                    currentTransaction = parseUtil.parseTransaction(record.get(1));
                    activeServerIds = parseUtil.parseActiveServerList(record.get(2));

//                    LocalDateTime startTime = LocalDateTime.now();
                    serverStatusUtil.setServerStatuses(activeServerIds);
//                    LocalDateTime currentTime = LocalDateTime.now();
//                    log.info("{}", Stopwatch.getDuration(startTime, currentTime, "Server status reset"));

                }
                else {
                    currentTransaction = parseUtil.parseTransaction(record.get(1));
                }

                url = restServerUrl+":"+(ports.get(0) - 1 + offset + currentTransaction.getSenderId())+"/transaction";

                sender_name = (char)(baseUname + currentTransaction.getSenderId() - 1);
                receiver_name = (char)(baseUname + currentTransaction.getReceiverId() - 1);

                CompletableFuture<Transaction> futureTransaction = apiService.asyncTransact(Character.toString(sender_name),
                        Character.toString(receiver_name),
                        currentTransaction.getAmount(),
                        url);

                log.info("Set {}: Executing transaction - {}:{}->{}", currentSetNumber, sender_name, receiver_name, currentTransaction.getAmount());

                futureTransaction.thenAccept(transaction -> {if(transaction!=null) log.info("Transaction executed: {}", transaction); else log.error("Transaction failed");});

            }
        }
        catch (IOException e) {
            log.error("Error reading csv file: {}", e.getMessage());
        }
        catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }
    }

    public boolean promptUser(BufferedReader reader) {
        boolean cont = false, exit = false;
        try {
            String input;
            while(true){
                if(cont || exit) break;
                System.out.println("""
                        Enter commands:
                        - printBalance <username (optional)> # Gets the balance of the client = {username} from its own server
                        - printLog <username (optional)> # Gets the logs of the server handling client = {username}
                        - printDB <username (optional)> # Gets the db at the server handling client = {username}
                        - performance
                        - continue | c
                        - exit | e
                        """);
                input = reader.readLine();

                if(input != null){
                    String[] parts = input.split(" ");

                    switch(parts[0]){
                        case "printBalance":
                            if(parts.length == 1){
                                System.out.println("Balance: $"+apiService.balanceCheck(clientService.getUserId()));
                            }
                            else if(parts.length == 2){
                                System.out.println("Balance: $"+apiService.balanceCheck(parts[1]));
                            }
                            else{
                                log.warn("Invalid command or username");
                            }
                            break;
                        case "printLog":

                            log.warn("In progress");

                            break;
                        case "printDB":

                            log.warn("In progress");

                            break;
                        case "performance":

                            log.warn("In progress");

                            break;
                        case "continue":
                            cont = true;
                            break;
                        case "c":
                            cont = true;
                            break;
                        case "exit":
                            exit = true;
                            break;
                        case "e":
                            exit = true;
                            break;
                        default:
                            log.warn("Unknown command: {}", parts[0]);
                            break;
                    }
                }
            }
        }
        catch(Exception e){
            log.trace("Error reading input: {}", e.getMessage());
        }
        return exit;
    }

    public Integer tryParseSetNumber(String value){
        try{
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e){
            return null;
        }
    }
}
