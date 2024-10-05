package com.lab.paxos.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

@Component
@Slf4j
public class CommandUtil {

    @Autowired
    SocketMessageUtil socketMessageUtil;

    public void listenForCommands(List<Integer> PORT_POOL, int assignedPort) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String input;
            while (true) {
                log.info("Enter commands: b (broadcast) <message> | s (send) <port> <message>");
                input = reader.readLine();

                if (input != null) {
                    String[] parts = input.split(" ");

                    switch (parts[0]) {
                        case "b":
                            socketMessageUtil.broadcast(PORT_POOL, assignedPort, parts[1]);
                            break;
                        case "s":
                            int targetPort = -1;
                            try {
                                targetPort = Integer.parseInt(parts[1]);
                                socketMessageUtil.sendMessageToServer(targetPort, parts[2]);
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
}
