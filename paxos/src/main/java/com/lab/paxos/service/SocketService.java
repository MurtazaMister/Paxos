package com.lab.paxos.service;

import com.lab.paxos.service.client.ClientService;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@Getter
@Setter
@NoArgsConstructor
public class SocketService {
    @Autowired
    private ClientService clientService;
    private List<Integer> PORT_POOL;

    private int assignedPort;

    @Value("${server.port.pool}")
    private String portPool;

    private ServerSocket serverSocket;

    @PostConstruct
    public void init() {
        log.info("PortPool: {}", portPool);
        // Split the comma-separated ports and convert to Integer list
        this.PORT_POOL = new ArrayList<>();
        Arrays.stream(portPool.split(","))
                .map(String::trim)  // Trim whitespace
                .map(Integer::parseInt)  // Convert to Integer
                .forEach(PORT_POOL::add);

        assignedPort = findAvailablePort();
        if(assignedPort == -1){
            log.warn("Converting to client");
        } else {
            log.info("Assigned port: {}", assignedPort);
            log.info("Starting ServerSocket");

            try{
                serverSocket = new ServerSocket(assignedPort);
                log.info("Server listening on port: {}", assignedPort);
            }
            catch (IOException e){
                log.trace("IOException: {}", e.getMessage());
            }
        }

    }

    public void startServerSocket(){
        if(assignedPort != -1){
            log.info("Socket open for incoming connections and commands");

            // A thread to listen for commands on the terminal
            new Thread(this::listenForCommands).start();

            // A thread to listen for incoming messages
            listenForIncomingMessages(serverSocket);

        }
        else{
            clientService.startClient();
        }
    }

    private int findAvailablePort(){
        for(int port : PORT_POOL){
            if(isPortAvailable(port)){
                return port;
            }
        }
        return -1;
    }

    private boolean isPortAvailable(int port){
        try(ServerSocket serverSocket = new ServerSocket(port)){
            return true; // Port is available
        } catch (Exception e) {
            return false; // Port unavailable
        }
    }

    private void listenForIncomingMessages(ServerSocket serverSocket){
        try{
            while(true){
                Socket incoming = serverSocket.accept();
                new Thread(() -> handleIncomingMessage(incoming)).start();
            }
        }
        catch(IOException e){
            log.trace("Error while listening for incoming messages\n{}", e.getMessage());
        }
    }

    private void handleIncomingMessage(Socket incoming){
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
            PrintWriter out = new PrintWriter(incoming.getOutputStream(), true);

            String message;
            while((message = in.readLine()) != null){ // incoming message from another server
                log.info("Received message: \"{}\"", message);
                out.println("Acknowledged: "+message);
                log.info("Sent ACK for: {}", message);
            }
        }
        catch(IOException e) {
            log.trace("IOException: {}", e.getMessage());
        }
        finally {
            try{
                incoming.close();
            }
            catch(IOException e){
                log.trace("IOException: {}", e.getMessage());
            }
        }
    }

    private void listenForCommands(){
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))){
            String input;
            while(true){
                log.info("Enter commands: b (broadcast) <message> | s (send) <port> <message>");
                input = reader.readLine();

                if(input != null) {
                    String[] parts = input.split(" ");

                    switch (parts[0]) {
                        case "b":
                            broadcast(parts[1]);
                            break;
                        case "s":
                            int targetPort = -1;
                            try {
                                targetPort = Integer.parseInt(parts[1]);
                                sendMessageToServer(targetPort, parts[2]);
                            } catch (NumberFormatException e) {
                                log.warn("Invalid port number: {}", targetPort);
                            }
                            break;
                        default:
                            log.warn("Unknown command: {}", parts[0]);
                            break;
                    }
                }
            }
        } catch (Exception e) {
            log.trace("Error reading input: {}", e.getMessage());
        }
    }

    public void broadcast(String message){
        for(int port : PORT_POOL){
            if(port != assignedPort){
                sendMessageToServer(port, message);
            }
        }
    }

    public void sendMessageToServer(int port, String message){
        try (Socket socket = new Socket("localhost", port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println(message);
            log.info("Sent message to port {}: {}", port, message);
            String ack = in.readLine();
            log.info("Received ACK from port {}: {}", port, ack);
        } catch (IOException e) {
            log.error("Failed to send message to port {}: {}", port, e.getMessage());
        }
    }
}
