package com.lab.paxos.wrapper;

import com.lab.paxos.networkObjects.acknowledgements.AckMessage;
import com.lab.paxos.networkObjects.acknowledgements.AckServerStatusUpdate;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class AckMessageWrapper implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum MessageType {
        ACK_SERVER_STATUS_UPDATE,
        ACK_MESSAGE,
        PROMISE,
        ACCEPTED
    }

    private MessageType type;
    private AckServerStatusUpdate ackServerStatusUpdate;
    private AckMessage ackMessage;

    public AckMessageWrapper(MessageType type, AckServerStatusUpdate ackServerStatusUpdate) {
        this.type = type;
        this.ackServerStatusUpdate = ackServerStatusUpdate;
    }

    public AckMessageWrapper(MessageType type, AckMessage ackMessage) {
        this.type = type;
        this.ackMessage = ackMessage;
    }
}
