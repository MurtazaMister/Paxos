package com.lab.paxos.wrapper;

import com.lab.paxos.networkObjects.communique.Message;
import com.lab.paxos.networkObjects.communique.Prepare;
import com.lab.paxos.networkObjects.communique.ServerStatusUpdate;
import lombok.*;

import java.io.Serializable;

@Getter
@Builder
@Setter
public class SocketMessageWrapper implements Serializable {

    public static SocketMessageWrapper from(SocketMessageWrapper socketMessageWrapper){
        return SocketMessageWrapper.builder()
                .type(socketMessageWrapper.getType())
                .fromPort(socketMessageWrapper.getFromPort())
                .toPort(socketMessageWrapper.getToPort())
                .message(socketMessageWrapper.getMessage())
                .serverStatusUpdate(socketMessageWrapper.getServerStatusUpdate())
                .prepare(socketMessageWrapper.getPrepare())
                .build();
    }

    private static final long serialVersionUID = 1L;

    public enum MessageType {
        SERVER_STATUS_UPDATE,
        MESSAGE,
        PREPARE,
        ACCEPT,
        DECIDE
    }

    private MessageType type;

    private int fromPort;
    private int toPort;

    private ServerStatusUpdate serverStatusUpdate;
    private Message message;
    private Prepare prepare;
}
