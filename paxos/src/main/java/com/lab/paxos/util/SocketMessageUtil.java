package com.lab.paxos.util;

import com.lab.paxos.networkObjects.acknowledgements.AckMessage;
import com.lab.paxos.networkObjects.acknowledgements.AckServerStatusUpdate;
import com.lab.paxos.networkObjects.communique.Message;
import com.lab.paxos.networkObjects.communique.ServerStatusUpdate;
import com.lab.paxos.service.SocketService;
import com.lab.paxos.wrapper.AckMessageWrapper;
import com.lab.paxos.wrapper.SocketMessageWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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

    @Autowired
    ServerStatusUtil serverStatusUtil;

    @Autowired
    @Lazy
    SocketService socketService;

    public AckMessageWrapper sendMessageToServer(int port, SocketMessageWrapper message) throws IOException {

        if(serverStatusUtil.isFailed()){
            throw new IOException("Server Unavailable");
        }

        AckMessageWrapper ackMessageWrapper = null;
        try(Socket socket = new Socket("localhost", port);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())){

            out.writeObject(message);
            out.flush();

            // Receiving acknowledgement from the respective server
            ackMessageWrapper = (AckMessageWrapper) in.readObject();
            ackDisplayUtil.displayAcknowledgement(ackMessageWrapper);

        }
        catch (IOException | ClassNotFoundException e){
            log.trace("Failed to send message to port {}: {}", port, e.getMessage());
        }

        return ackMessageWrapper;
    }

    public void broadcast(List<Integer> PORT_POOL, int assignedPort, String message) throws IOException {

        if(serverStatusUtil.isFailed()){
            throw new IOException("Server Unavailable");
        }

        for (int port : PORT_POOL) {
            if (port != assignedPort) {
                Message mess = new Message(message, assignedPort, port);
                SocketMessageWrapper smw = new SocketMessageWrapper(SocketMessageWrapper.MessageType.MESSAGE, mess);
                sendMessageToServer(port, smw);
            }
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

            ObjectInputStream in = new ObjectInputStream(incoming.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(incoming.getOutputStream());

            SocketMessageWrapper message;
            while ((message = (SocketMessageWrapper) in.readObject()) != null) { // incoming message from another server

                // failed server will accept a resume request
                if(serverStatusUtil.isFailed()){
                    if(!(message.getType() == SocketMessageWrapper.MessageType.SERVER_STATUS_UPDATE && !message.getServerStatusUpdate().isFailServer())){
                        log.error("Rejecting incoming message, current server down");
                        incoming.close();
                        return;
                    }
                }

                switch (message.getType()){
                    case SERVER_STATUS_UPDATE:
                        ServerStatusUpdate serverStatusUpdate = message.getServerStatusUpdate();
                        if(serverStatusUpdate.getToPort() == socketService.getAssignedPort()){
                            log.info("Received from port {}: {}", serverStatusUpdate.getFromPort(), serverStatusUpdate);

                            serverStatusUtil.setFailed(serverStatusUpdate.isFailServer());

                            AckServerStatusUpdate ackServerStatusUpdate = new AckServerStatusUpdate(serverStatusUtil.isFailed(), socketService.getAssignedPort(), serverStatusUpdate.getFromPort());
                            AckMessageWrapper ackMessageWrapper = new AckMessageWrapper(AckMessageWrapper.MessageType.ACK_SERVER_STATUS_UPDATE, ackServerStatusUpdate);

                            out.writeObject(ackMessageWrapper);

                            log.info("Sent ACK to server {}: {}", ackServerStatusUpdate.getToPort(), ackMessageWrapper.getAckServerStatusUpdate());

                            out.flush();
                        }
                        break;
                    case MESSAGE:
                        Message mess = message.getMessage();
                        log.info("Received from port {}: {}", mess.getFromPort(), mess);

                        AckMessage ackMessage = new AckMessage(mess.getMessage(), mess.getToPort(), mess.getFromPort());
                        AckMessageWrapper ackMessageWrapper = new AckMessageWrapper(AckMessageWrapper.MessageType.ACK_MESSAGE, ackMessage);

                        out.writeObject(ackMessageWrapper);

                        log.info("Sent ACK to server {}: {}", ackMessage.getFromPort(), ackMessage);

                        out.flush();
                        break;
                }

            }
        } catch (IOException | ClassNotFoundException e) {
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
