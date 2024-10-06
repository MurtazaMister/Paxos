package com.lab.paxos.wrapper;

import com.lab.paxos.model.network.AckServerStatusUpdate;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class AckMessageWrapper implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum MessageType {
        ACK_SERVER_STATUS_UPDATE,
        // other message types
    }

    private MessageType type;
    private AckServerStatusUpdate ackServerStatusUpdate;

    public AckMessageWrapper(MessageType type, AckServerStatusUpdate ackServerStatusUpdate) {
        this.type = type;
        this.ackServerStatusUpdate = ackServerStatusUpdate;
    }
}
