package com.lab.paxos.wrapper;

import com.lab.paxos.model.network.AckServerStatusUpdate;
import com.lab.paxos.model.network.ServerStatusUpdate;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class SocketMessageWrapper implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum MessageType {
        SERVER_STATUS_UPDATE,
        // other message types
    }

    private MessageType type;
    private ServerStatusUpdate serverStatusUpdate;

    public SocketMessageWrapper(MessageType type, ServerStatusUpdate serverStatusUpdate) {
        this.type = type;
        this.serverStatusUpdate = serverStatusUpdate;
    }
}
