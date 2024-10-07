package com.lab.paxos.wrapper;

import com.lab.paxos.model.network.communique.Message;
import com.lab.paxos.model.network.communique.ServerStatusUpdate;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class SocketMessageWrapper implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum MessageType {
        SERVER_STATUS_UPDATE,
        MESSAGE,
        // other message types
    }

    private MessageType type;
    private ServerStatusUpdate serverStatusUpdate;
    private Message message;

    public SocketMessageWrapper(MessageType type, ServerStatusUpdate serverStatusUpdate) {
        this.type = type;
        this.serverStatusUpdate = serverStatusUpdate;
    }

    public SocketMessageWrapper(MessageType type, Message message) {
        this.type = type;
        this.message = message;
    }
}
