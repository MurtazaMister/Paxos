package com.lab.paxos.util;

import com.lab.paxos.model.network.AckServerStatusUpdate;
import com.lab.paxos.wrapper.AckMessageWrapper;
import com.lab.paxos.wrapper.SocketMessageWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

@Component
@Slf4j
public class SocketMessageUtil {

    @Autowired
    AckDisplayUtil ackDisplayUtil;

    public void broadcast(List<Integer> PORT_POOL, int assignedPort, SocketMessageWrapper message) {
        for (int port : PORT_POOL) {
            if (port != assignedPort) {
                sendMessageToServer(port, message);
            }
        }
    }

    public AckMessageWrapper sendMessageToServer(int port, SocketMessageWrapper message) {
        AckMessageWrapper ackMessageWrapper = null;
        try(Socket socket = new Socket("localhost", port);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream())){

            out.writeObject(message);
            out.flush();

            log.info("Sent message to port {}: {}", port, message);

            // Receiving acknowledgement from the respective server
            ackMessageWrapper = (AckMessageWrapper) in.readObject();
            ackDisplayUtil.displayAcknowledgement(ackMessageWrapper);

        }
        catch (IOException | ClassNotFoundException e){
            log.trace("Failed to send message to port {}: {}", port, e.getMessage());
        }

        return ackMessageWrapper;
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
