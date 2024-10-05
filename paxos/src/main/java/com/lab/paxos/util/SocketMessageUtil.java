package com.lab.paxos.util;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

@Component
@Slf4j
public class SocketMessageUtil {

    public void broadcast(List<Integer> PORT_POOL, int assignedPort, String message) {
        for (int port : PORT_POOL) {
            if (port != assignedPort) {
                sendMessageToServer(port, message);
            }
        }
    }

    public void sendMessageToServer(int port, String message) {
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

    public void listenForIncomingMessages(@NotNull ServerSocket serverSocket) {
        try {
            while (true) {
                Socket incoming = serverSocket.accept();
                new Thread(() -> handleIncomingMessage(incoming)).start();
            }
        } catch (IOException e) {
            log.trace("Error while listening for incoming messages\n{}", e.getMessage());
        }
    }

    private void handleIncomingMessage(@NotNull Socket incoming) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
            PrintWriter out = new PrintWriter(incoming.getOutputStream(), true);

            String message;
            while ((message = in.readLine()) != null) { // incoming message from another server
                log.info("Received message: \"{}\"", message);
                out.println("Acknowledged: " + message);
                log.info("Sent ACK for: {}", message);
            }
        } catch (IOException e) {
            log.trace("IOException: {}", e.getMessage());
        } finally {
            try {
                incoming.close();
            } catch (IOException e) {
                log.trace("IOException: {}", e.getMessage());
            }
        }
    }
}
