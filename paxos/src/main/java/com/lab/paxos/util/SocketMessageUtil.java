package com.lab.paxos.util;

import com.lab.paxos.config.SocketConfig;
import com.lab.paxos.networkObjects.acknowledgements.AckMessage;
import com.lab.paxos.networkObjects.acknowledgements.AckServerStatusUpdate;
import com.lab.paxos.networkObjects.communique.Message;
import com.lab.paxos.networkObjects.communique.ServerStatusUpdate;
import com.lab.paxos.service.PaxosService;
import com.lab.paxos.service.SocketService;
import com.lab.paxos.wrapper.AckMessageWrapper;
import com.lab.paxos.wrapper.SocketMessageWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SocketMessageUtil {

    @Autowired
    AckDisplayUtil ackDisplayUtil;

    @Autowired
    ServerStatusUtil serverStatusUtil;

    @Autowired
    PortUtil portUtil;

    @Autowired
    SocketConfig socketConfig;

    @Autowired
    PaxosService paxosService;

    @Autowired
    @Lazy
    SocketService socketService;

    public AckMessageWrapper sendMessageToServer(int targetPort, SocketMessageWrapper message) throws IOException {

//        if(serverStatusUtil.isFailed()){
//            throw new IOException("Server Unavailable");
//        }

        AckMessageWrapper ackMessageWrapper = null;
        try(Socket socket = new Socket()){

            // setting connection timeout
            socket.connect(new InetSocketAddress("localhost", targetPort), socketConfig.getConnectionTimeout());
            // setting timeout for receiving ack
            socket.setSoTimeout(socketConfig.getReadTimeout());

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());


            out.writeObject(message);
            out.flush();

            try{
                // Receiving acknowledgement from the respective server
                ackMessageWrapper = (AckMessageWrapper) in.readObject();
                ackDisplayUtil.displayAcknowledgement(ackMessageWrapper);
            } catch (SocketTimeoutException e) {
                log.error("Timeout after {} ms waiting for port {}", socketConfig.getReadTimeout(), targetPort);
            } catch (EOFException e) {
                log.error("Connection closed by the server without sending ack for port {}: {}", targetPort, e.toString());
            } catch (IOException e) {
                log.error("IO Error receiving ack message from port {}: {}", targetPort, e.toString());
            } catch (Exception e) {
                log.error("Unexpected error receiving ack message from port {}: {}", targetPort, e.toString());
            }

        }
        catch (IOException e){
            log.error("Failed to send message to port {}: {}", targetPort, e.getMessage());
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
        }
        if(ackMessageWrapper == null) throw new IOException("Service Unavailable");
        return ackMessageWrapper;
    }

    public CompletableFuture<List<AckMessageWrapper>> broadcast(SocketMessageWrapper socketMessageWrapper, List<Integer> PORT_POOL) throws IOException{
//        if(serverStatusUtil.isFailed()){
//            return CompletableFuture.failedFuture(new IOException("Server Unavailable"));
//        }

        int assignedPort = socketService.getAssignedPort();

        List<CompletableFuture<AckMessageWrapper>> futures = PORT_POOL.stream()
                .filter(port -> port != assignedPort)
                .map(port -> CompletableFuture.supplyAsync(() -> {
                    try{
                        SocketMessageWrapper smw = SocketMessageWrapper.from(socketMessageWrapper);
                        smw.setToPort(port);
                        return sendMessageToServer(port, smw);
                    }
                    catch(IOException e){
                        log.error("IOException {}: {}", port, e.getMessage());
                        return null;
                    }
                    catch(Exception e){
                        log.error("Failed to send message to port {}: {}", port, e.getMessage());
                        return null;
                    }
                }))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .filter(ack -> ack!=null)
                        .collect(Collectors.toList()));


    }

    public CompletableFuture<List<AckMessageWrapper>> broadcast(SocketMessageWrapper socketMessageWrapper) throws IOException {
        List<Integer> PORT_POOL = portUtil.portPoolGenerator();
        return broadcast(socketMessageWrapper, PORT_POOL);
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

            ObjectOutputStream out = new ObjectOutputStream(incoming.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(incoming.getInputStream());

            SocketMessageWrapper message;
            while ((message = (SocketMessageWrapper) in.readObject()) != null) { // incoming message from another server

                switch (message.getType()){
                    case SERVER_STATUS_UPDATE:
                        ServerStatusUpdate serverStatusUpdate = message.getServerStatusUpdate();
                        if(message.getToPort() == socketService.getAssignedPort()){
                            log.info("Received from port {}: {}", message.getFromPort(), serverStatusUpdate);

                            serverStatusUtil.setFailed(serverStatusUpdate.isFailServer());

                            AckServerStatusUpdate ackServerStatusUpdate = AckServerStatusUpdate.builder()
                                    .serverFailed(serverStatusUtil.isFailed())
                                    .build();

                            AckMessageWrapper ackMessageWrapper = AckMessageWrapper.builder()
                                    .type(AckMessageWrapper.MessageType.ACK_SERVER_STATUS_UPDATE)
                                    .ackServerStatusUpdate(ackServerStatusUpdate)
                                    .fromPort(socketService.getAssignedPort())
                                    .toPort(message.getFromPort())
                                    .build();

                            out.writeObject(ackMessageWrapper);

                            log.info("Sent ACK to server {}: {}", ackMessageWrapper.getToPort(), ackMessageWrapper.getAckServerStatusUpdate());

                            out.flush();
                        }
                        else{
                            log.info("Target port does not match port of current server");

                            AckServerStatusUpdate ackServerStatusUpdate = AckServerStatusUpdate.builder()
                                    .serverFailed(serverStatusUtil.isFailed())
                                    .build();

                            AckMessageWrapper ackMessageWrapper = AckMessageWrapper.builder()
                                    .type(AckMessageWrapper.MessageType.ACK_SERVER_STATUS_UPDATE)
                                    .ackServerStatusUpdate(ackServerStatusUpdate)
                                    .fromPort(socketService.getAssignedPort())
                                    .toPort(message.getFromPort())
                                    .build();

                            out.writeObject(ackMessageWrapper);

                            log.info("Sent ACK to server {}: {}", ackMessageWrapper.getToPort(), ackMessageWrapper.getAckServerStatusUpdate());

                            out.flush();
                        }
                        break;
                    case MESSAGE:
                        Message mess = message.getMessage();
                        log.info("Received from port {}: {}", message.getFromPort(), mess);

                        AckMessage ackMessage = AckMessage.builder()
                                .message(mess.getMessage())
                                .build();

                        AckMessageWrapper ackMessageWrapper = AckMessageWrapper.builder()
                                .type(AckMessageWrapper.MessageType.ACK_MESSAGE)
                                .ackMessage(ackMessage)
                                .fromPort(message.getToPort())
                                .toPort(message.getFromPort())
                                .build();

                        out.writeObject(ackMessageWrapper);

                        log.info("Sent ACK to server {}: {}", ackMessageWrapper.getToPort(), ackMessage);

                        out.flush();
                        break;

                    case PREPARE:
                        if(serverStatusUtil.isFailed()) return;
                        paxosService.promise(in, out, message);
                        out.flush();
                        break;

                    case ACCEPT:
                        if(serverStatusUtil.isFailed()) return;
                        paxosService.accepted(in, out, message);
                        out.flush();
                        break;

                    case DECIDE:
                        if(serverStatusUtil.isFailed()) return;
                        paxosService.commit(socketService.getAssignedPort(), in, out, message);
                        out.flush();
                        break;

                    case SYNC:
                        if(serverStatusUtil.isFailed()) return;
                        paxosService.ackSync(socketService.getAssignedPort(), in, out, message);
                        break;

                    case UPDATE:
                        if(serverStatusUtil.isFailed()) return;
                        paxosService.ackUpdate(socketService.getAssignedPort(), in, out, message);
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            // log.error("IOException | ClassNotFoundException: {}", e.toString());
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
        } finally {
            try {
                incoming.close();
            } catch (IOException e) {
                log.error("IOException: {}", e.getMessage());
            }
        }
    }
}
