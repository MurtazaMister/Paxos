package com.lab.paxos.service;

import com.lab.paxos.service.client.ClientService;
import com.lab.paxos.util.CommandUtil;
import com.lab.paxos.util.PortUtil;
import com.lab.paxos.util.ServerStatusUtil;
import com.lab.paxos.util.SocketMessageUtil;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

@Service
@Slf4j
@Getter
@Setter
@NoArgsConstructor
public class SocketService {

    @Autowired
    private CommandUtil commandUtil;

    @Autowired
    private PortUtil portUtil;

    @Autowired
    private SocketMessageUtil socketMessageUtil;

    @Autowired
    private ClientService clientService;

    private List<Integer> PORT_POOL;

    private int assignedPort;

    private ServerSocket serverSocket;

    @PostConstruct
    public void init() {

        this.PORT_POOL = portUtil.portPoolGenerator();

        assignedPort = portUtil.findAvailablePort(PORT_POOL);

        if (assignedPort == -1) {
            log.warn("Converting to client");
        } else {
            log.info("Assigned port: {}", assignedPort);
            log.info("Starting ServerSocket");

            try {
                serverSocket = new ServerSocket(assignedPort);
                log.info("Server listening on port: {}", assignedPort);
            } catch (IOException e) {
                log.trace("IOException: {}", e.getMessage());
            }
        }

    }

    public void startServerSocket() {
        if (assignedPort != -1) {
            log.info("Socket open for incoming connections and commands");

            // A thread to listen for commands on the terminal
            new Thread(() -> commandUtil.listenForCommands(assignedPort)).start();

            // A thread to listen for incoming messages
            socketMessageUtil.listenForIncomingMessages(serverSocket);

        } else {
            clientService.startClient();
        }
    }


}
